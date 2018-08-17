package de.exb.platform.cloud.fileservice.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.glassfish.jersey.internal.guava.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import de.exb.platform.cloud.fileservice.properties.StorageProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

	private static final String ERROR_APATH = "aPath can not be empty";
	private static final String LOG_CALLED_PATH_SESSIONID = "{} called for path:{} and sessionId: {}";
	private static final String LOG_CALLED_PATH = "{} called for path:{}";
	private static final String LOG_CALLED_SESSIONID = "{} called for sessionId: {}";

	private final Path rootLocation;

	/**
	 * @param properties keeps rootLocation information
	 */
	@Autowired
	public FileServiceImpl(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	/**
	 * @throws IOException for failure case
	 */
	@PostConstruct
	protected void init() throws IOException {
		Path createdFile = Files.createDirectories(rootLocation);
		validate(createdFile.toFile().exists(), String.format("Could not created: %s", rootLocation.toString()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.exb.platform.cloud.fileservice.service.FileService#openForWriting(java.
	 * lang.String, java.lang.String, boolean)
	 */
	@SneakyThrows(FileNotFoundException.class)
	@Override
	public OutputStream openForWriting(final String aSessionId, final String aPath, final boolean aAppend)
			throws FileServiceException {
		log.info(LOG_CALLED_PATH_SESSIONID, "openForWriting", aPath, aSessionId);
		Path path = validateExistPath(aPath);
		return new FileOutputStream(path.toFile(), aAppend);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.exb.platform.cloud.fileservice.service.FileService#openForReading(java.
	 * lang.String, java.lang.String)
	 */
	@SneakyThrows(FileNotFoundException.class)
	@Override
	public InputStream openForReading(final String aSessionId, final String aPath) throws FileServiceException {
		log.info(LOG_CALLED_PATH_SESSIONID, "openForReading", aPath, aSessionId);
		Path path = validateExistPath(aPath);
		return new FileInputStream(path.toFile());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.exb.platform.cloud.fileservice.service.FileService#getLength(java.lang.
	 * String)
	 */
	@Override
	public @NotNull long getLength(@NotNull String aPath) throws FileServiceException {
		log.info(LOG_CALLED_PATH, "getLength", aPath);
		Path path = validateExistPath(aPath);
		return path.toFile().length();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.exb.platform.cloud.fileservice.service.FileService#list(java.lang.String,
	 * java.lang.String)
	 */
	@SneakyThrows(IOException.class)
	@Override
	public Stream<Path> list(final String aSessionId, final String aPath) throws FileServiceException {
		log.info(LOG_CALLED_PATH_SESSIONID, "list", aPath, aSessionId);
		validate(aPath);
		Path path = Paths.get(this.rootLocation.toFile().getAbsolutePath(), aPath);
		return path.toFile().exists() ? Files.walk(path).filter(p -> p.toFile().isFile()) : Stream.empty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.exb.platform.cloud.fileservice.service.FileService#listFiles(java.lang.
	 * String, java.lang.String)
	 */
	@SneakyThrows(IOException.class)
	@Override
	public Stream<String> listFiles(final String aSessionId, final String aPath) throws FileServiceException {
		log.info(LOG_CALLED_PATH_SESSIONID, "listFiles", aPath, aSessionId);
		Stream<Path> paths = list(aSessionId, aPath);
		String rootPath = String.format("%s/", rootLocation.toFile().getCanonicalPath());
		return paths.map(p -> getPath(p, rootPath));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.exb.platform.cloud.fileservice.service.FileService#delete(java.lang.
	 * String, java.lang.String, boolean)
	 */
	@SneakyThrows(IOException.class)
	@Override
	public void delete(final String aSessionId, final String aPath, final boolean aRecursive)
			throws FileServiceException {
		log.info(LOG_CALLED_PATH_SESSIONID, "delete", aPath, aSessionId);
		final Path path = validateExistPath(aPath);
		if (!aRecursive) {
			Files.delete(path);
			return;
		}

		// delete files
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(final Path aFile, final BasicFileAttributes aAttrs) throws IOException {
				Files.delete(aFile);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path aDir, final IOException aExc) throws IOException {
				Files.delete(aDir);
				return FileVisitResult.CONTINUE;
			}
		});

		// delete empty folders
		for (Path parent = path.getParent(); !this.rootLocation.equals(parent); parent = parent.getParent()) {
			if (parent == null) {
				break;
			}
			String[] subfiles = parent.toFile().list();
			if (subfiles == null || subfiles.length == 0) {
				log.info("Deleting empty parent: {}", parent.toString());
				Files.delete(parent);
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.exb.platform.cloud.fileservice.service.FileService#exists(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public boolean exists(final String aSessionId, final String aPath) throws FileServiceException {
		log.info(LOG_CALLED_PATH_SESSIONID, "exists", aPath, aSessionId);
		validate(aPath);
		return Paths.get(this.rootLocation.toString(), aPath).toFile().exists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.exb.platform.cloud.fileservice.service.FileService#getParent(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public String getParent(final String aSessionId, final String aPath) throws FileServiceException {
		log.info(LOG_CALLED_PATH_SESSIONID, "getParent", aPath, aSessionId);
		Path path = validateExistPath(aPath);
		Path parent = path.getParent();
		return parent == null ? null : parent.toFile().getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.exb.platform.cloud.fileservice.service.FileService#store(java.lang.String,
	 * org.springframework.web.multipart.MultipartFile)
	 */
	@SneakyThrows(IOException.class)
	@Override
	public void store(final String aSessionId, final MultipartFile file) throws FileServiceException {
		log.info(LOG_CALLED_SESSIONID, "store", aSessionId);
		validate(file);

		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		log.info(LOG_CALLED_PATH, "store", filename);
		validate(filename);
		validate(!file.isEmpty(), String.format("Failed to store empty file %s", filename));

		Path path = validateNotExistPath(filename);

		try (InputStream inputStream = file.getInputStream()) {
			Files.createDirectories(path.getParent());
			Files.copy(inputStream, this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * null validation
	 *
	 * @param file the file to validate
	 */
	private void validate(MultipartFile file) {
		Preconditions.checkArgument(file != null, "file can not be null");
	}

	/**
	 * null validation
	 *
	 * @param aPath the path to validate
	 */
	private void validate(String aPath) {
		Preconditions.checkArgument(StringUtils.hasText(aPath), ERROR_APATH);
		// This is a security check
		Preconditions.checkArgument(!aPath.contains(".."),
				String.format("Cannot store file with relative path outside current directory %s", aPath));

	}

	/**
	 * validate and return path
	 *
	 * @param aPath the path to validate
	 * @return a Path instance
	 */
	private Path validateExistPath(String aPath) {
		validate(aPath);
		Path path = Paths.get(this.rootLocation.toFile().getAbsolutePath(), aPath);
		validate(path.toFile().exists(), String.format("Not found %s", aPath));

		return path;
	}

	/**
	 * validate and return path
	 *
	 * @param aPath the path to validate
	 * @return a Path instance
	 */
	private Path validateNotExistPath(String aPath) {
		validate(aPath);
		Path path = Paths.get(this.rootLocation.toFile().getAbsolutePath(), aPath);
		validate(!path.toFile().exists(), String.format("Already exist %s", aPath));

		return path;
	}

	/**
	 * expression validation. It throws a FileServiceException if expression
	 * validation is failed.
	 *
	 * @param expression   the expression for validation
	 * @param errorMessage the error message for fail case
	 */
	@SneakyThrows(FileServiceException.class)
	private void validate(boolean expression, String errorMessage) {
		if (!expression) {
			throw new FileServiceException(String.valueOf(errorMessage));
		}
	}

	/**
	 * returns restricted path
	 * 
	 * @param p        file path
	 * @param rootPath root path value
	 * @return a restricted path value
	 */
	@SneakyThrows(IOException.class)
	private String getPath(Path p, String rootPath) {
		return p.toFile().getCanonicalPath().replace(rootPath, "");
	}

}

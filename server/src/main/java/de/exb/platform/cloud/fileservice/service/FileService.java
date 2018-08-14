package de.exb.platform.cloud.fileservice.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author resulav
 *
 */
public interface FileService {

	/**
	 * creates a writable stream for file
	 * 
	 * @param aSessionId the HTTP request sessionId
	 * @param aPath      the file path
	 * @param aAppend    append enabled or no
	 * @return a writable stream
	 * @throws FileServiceException as generic service exception
	 */
	@NotNull
	OutputStream openForWriting(@NotNull final String aSessionId, @NotNull final String aPath, boolean aAppend)
			throws FileServiceException;

	/**
	 * creates a readable stream for file
	 * 
	 * @param aSessionId the HTTP request sessionId
	 * @param aPath      the file path
	 * @return a readable stream
	 * @throws FileServiceException as generic service exception
	 */
	@NotNull
	InputStream openForReading(@NotNull final String aSessionId, @NotNull final String aPath)
			throws FileServiceException;

	/**
	 * returns a file's content size
	 * 
	 * @param aPath the file path
	 * @return size of content
	 * @throws FileServiceException as generic service exception
	 */
	@NotNull
	long getLength(@NotNull final String aPath) throws FileServiceException;

	/**
	 * retrieves file list under a folder
	 * 
	 * @param aSessionId the HTTP request sessionId
	 * @param aPath      the folder path
	 * @return a stream of file path
	 * @throws FileServiceException as generic service exception
	 */
	@NotNull
	Stream<Path> list(@NotNull final String aSessionId, @NotNull final String aPath) throws FileServiceException;

	/**
	 * retrieves file list under a folder
	 * 
	 * @param aSessionId the HTTP request sessionId
	 * @param aPath      aPath the folder path
	 * @return a stream of file path
	 * @throws FileServiceException as generic service exception
	 */
	@NotNull
	Stream<String> listFiles(final String aSessionId, final String aPath) throws FileServiceException;

	/**
	 * deletes files or folder
	 * 
	 * @param aSessionId the HTTP request sessionId
	 * @param aPath      the file or folder path
	 * @param aRecursive delete recursive or no
	 * @throws FileServiceException as generic service exception
	 */
	void delete(@NotNull final String aSessionId, @NotNull final String aPath, final boolean aRecursive)
			throws FileServiceException;

	/**
	 * returns a file or folder's existing information
	 * 
	 * @param aSessionId the HTTP request sessionId
	 * @param aPath      the file or folder path
	 * @return true if exist, false if not exist
	 * @throws FileServiceException as generic service exception
	 */
	boolean exists(@NotNull final String aSessionId, @NotNull final String aPath) throws FileServiceException;

	/**
	 * returns a file or folder's parent path
	 * 
	 * @param aSessionId the HTTP request sessionId
	 * @param aPath      the file or folder path
	 * @return the parent path
	 * @throws FileServiceException as generic service exception
	 */
	String getParent(@NotNull final String aSessionId, @NotNull final String aPath) throws FileServiceException;

	/**
	 * saves a file to file system
	 * 
	 * @param aSessionId the HTTP request sessionId
	 * @param file       the file path
	 * @throws FileServiceException as generic service exception
	 */
	void store(@NotNull final String aSessionId, @NotNull final MultipartFile file) throws FileServiceException;
}

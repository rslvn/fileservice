package de.exb.platform.cloud.fileservice.resources;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.glassfish.jersey.internal.guava.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.exb.platform.cloud.fileservice.api.Constants;
import de.exb.platform.cloud.fileservice.service.FileService;
import de.exb.platform.cloud.fileservice.service.FileServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * endpoint of file service
 * 
 * @author resulav
 *
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_FILECONTROLLER)
public class FileServiceController {

	private static final String CALLED_LOG = "{} called";
	private static final String CALLED_FOR_LOG = "{} called for {}";

	@Autowired
	private FileService fileService;

	/**
	 * accepts listing files request
	 * 
	 * @param aPath   the directory path of folder for listing files
	 * @param session session HTTP session
	 * @return files path list
	 */
	@SneakyThrows(FileServiceException.class)
	@RequestMapping(value = "/{aPath:.+}", method = RequestMethod.GET)
	public ResponseEntity<List<String>> list(@PathVariable("aPath") String aPath, HttpSession session) {
		log.info(CALLED_FOR_LOG, "list", aPath);
		String path = decode(aPath);
		return ResponseEntity.ok(fileService.listFiles(session.getId(), path).collect(Collectors.toList()));
	}

	/**
	 * accepts file delete request
	 * 
	 * @param aPath      the file or folder path for deletion
	 * @param aRecursive keeps recursive or no
	 * @param session    session HTTP session
	 * @return HTTP result code
	 */
	@SneakyThrows(FileServiceException.class)
	@RequestMapping(value = "/{aPath:.+}/{aRecursive}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> delete(@PathVariable("aPath") String aPath,
			@PathVariable("aRecursive") boolean aRecursive, HttpSession session) {
		log.info(CALLED_FOR_LOG, "delete", aPath);
		String path = decode(aPath);
		fileService.delete(session.getId(), path, aRecursive);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * accepts store file requests
	 * 
	 * @param file    the file to store
	 * @param session HTTP session
	 * @return HTTP result code
	 */
	@SneakyThrows(FileServiceException.class)
	@RequestMapping(value = Constants.API_METHOD_UPLOAD, method = RequestMethod.POST)
	public ResponseEntity<Void> upload(@RequestPart("file") MultipartFile file, HttpSession session) {
		log.info(CALLED_LOG, "upload");
		fileService.store(session.getId(), file);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * accepts download file request
	 * 
	 * @param aPath   file path for downloading
	 * @param session session HTTP session
	 * @param request the HTTP request
	 * @return the file's contents
	 */
	@SneakyThrows(FileServiceException.class)
	@RequestMapping(value = Constants.API_METHOD_DOWNLOAD + "/{aPath:.+}", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> download(@PathVariable("aPath") String aPath, HttpSession session,
			HttpServletRequest request) {
		log.info(CALLED_FOR_LOG, "download", aPath);
		String path = decode(aPath);
		InputStream stream = fileService.openForReading(session.getId(), path);
		Preconditions.checkNotNull(stream, "InputStream can not be null");
		InputStreamResource resource = new InputStreamResource(stream);
		MediaType contentType = getContentType(request, path);
		long contentLength = fileService.getLength(path);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", path))
				.contentLength(contentLength).contentType(contentType).body(resource);
	}

	/**
	 * retrieves the MediaType from request If possible. Otherwise returns the
	 * default
	 * 
	 * @param request the HTTP request
	 * @param path    the file path
	 * @return a MediaType
	 */
	private MediaType getContentType(HttpServletRequest request, String path) {
		// Try to determine file's content type
		String contentType = request.getServletContext().getMimeType(path);
		// Fallback to the default content type if type could not be determined
		return contentType != null ? MediaType.valueOf(contentType) : MediaType.APPLICATION_OCTET_STREAM;
	}

	/**
	 * decodes the escaped path value
	 * 
	 * @param aPath
	 * @return
	 */
	@SneakyThrows(UnsupportedEncodingException.class)
	private String decode(String aPath) {
		return URLDecoder.decode(aPath, "UTF-8");
	}
}

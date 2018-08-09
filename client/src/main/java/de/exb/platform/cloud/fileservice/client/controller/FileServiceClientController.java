package de.exb.platform.cloud.fileservice.client.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.exb.platform.cloud.fileservice.api.Constants;
import de.exb.platform.cloud.fileservice.client.feign.FileServiceClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(Constants.API_CLIENT_FILECONTROLLER)
public class FileServiceClientController {

	@Autowired
	private FileServiceClient fileServiceClient;

	/**
	 * calls fileservice for listing files
	 * 
	 * @param path the folder that contains files
	 * @return file list
	 */
	@RequestMapping(value = "/{path:.+}", method = RequestMethod.GET)
	public List<String> list(@PathVariable("path") String path) {
		log.info("{} is called for {}", Constants.API_CLIENT_FILECONTROLLER, "list");
		return fileServiceClient.listFiles(path);
	}

	/**
	 * calls fileservice for deleting a file or folder
	 * 
	 * @param path      the file or folder for deletion
	 * @param recursive recursive or no
	 */
	@RequestMapping(value = "/{path:.+}/{recursive}", method = RequestMethod.DELETE)
	public void delete(@PathVariable("path") String path, @PathVariable("recursive") boolean recursive) {
		log.info("{} is called for {}", Constants.API_CLIENT_FILECONTROLLER, "download");
		fileServiceClient.deleteFile(path, recursive);
	}

	/**
	 * calls fileservice for downloading a file
	 * 
	 * @param path the file that is downloading
	 * @return file's content
	 */
	@SneakyThrows
	@RequestMapping(value = Constants.API_METHOD_DOWNLOAD + "/{path:.+}", method = RequestMethod.GET)
	public ResponseEntity<Resource> download(@PathVariable("path") String path, HttpServletRequest request) {
		log.info("{} is called for {}", Constants.API_CLIENT_FILECONTROLLER, "download");
		MultipartFile stream = fileServiceClient.downloadFile(path);
		MediaType contentType = getContentType(request, path);

		return ResponseEntity.ok().contentType(contentType)
				.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", path))
				.body(new InputStreamResource(stream.getInputStream()));
	}

	/**
	 * calls fileservice for uploading a file
	 * 
	 */
	@SneakyThrows
	@RequestMapping(value = Constants.API_METHOD_UPLOAD, method = RequestMethod.POST)
	public void upload(@RequestParam("file") MultipartFile file) {
		log.info("{} is called for {}", Constants.API_CLIENT_FILECONTROLLER, "upload");
		fileServiceClient.uploadFile(file);
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
}

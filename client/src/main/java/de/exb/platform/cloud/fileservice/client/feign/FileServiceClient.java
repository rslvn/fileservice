/**
 *
 */
package de.exb.platform.cloud.fileservice.client.feign;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Preconditions;

import de.exb.platform.cloud.fileservice.api.Constants;
import feign.Headers;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

/**
 * defines how to call file service methods
 *
 * @author resulav
 */
@FeignClient(name = Constants.APPLICATION_FILESERVICE, configuration = {
		FileServiceClient.MultipartSupportConfig.class })
public interface FileServiceClient {

	/**
	 * defines how to call listing files
	 *
	 * @param path the folder for listing file
	 * @return list of the files
	 */
	@RequestMapping(value = Constants.API_FILECONTROLLER + "/{path}", method = RequestMethod.GET)
	List<String> listFiles(@PathVariable("path") String path);

	/**
	 * defines how to call downloading a file
	 *
	 * @param path the file for downloading
	 * @return the contents of the file
	 */
	@RequestMapping(value = Constants.API_FILECONTROLLER + "/" + Constants.API_METHOD_DOWNLOAD
			+ "/{path}", method = RequestMethod.GET)
	MultipartFile downloadFile(@PathVariable("path") String path);

	/**
	 * defines how to call uploading a file
	 *
	 * @param file the file for uploading
	 */
	@RequestMapping(value = Constants.API_FILECONTROLLER + "/"
			+ Constants.API_METHOD_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.POST)
	@Headers("Content-Type: multipart/form-data")
	void uploadFile(@RequestParam("file") MultipartFile file);

	/**
	 * defines how to call deleting a file or folder
	 *
	 * @param path      the file or folder for deletion
	 * @param recursive recursive or no
	 */
	@RequestMapping(value = Constants.API_FILECONTROLLER + "/{path}/{recursive}", method = RequestMethod.DELETE)
	void deleteFile(@PathVariable("path") String path, @PathVariable("recursive") boolean recursive);

	/**
	 * @author resulav
	 */
	public class MultipartSupportConfig {

		@Autowired
		ObjectFactory<HttpMessageConverters> messageConverters;

		@Bean
		@Primary
		@Scope("prototype")
		public Encoder feignFormEncoder() {
			return new SpringFormEncoder();
		}

		@Bean
		@Primary
		@Scope("prototype")
		public Decoder decoder() {
			@SuppressWarnings("rawtypes")
			Decoder decoder = (response, type) -> {
				if (type instanceof Class && MultipartFile.class.isAssignableFrom((Class) type)) {
					String contentType = response.headers().get(HttpHeaders.CONTENT_TYPE).stream().findFirst()
							.orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

					Optional<String> headerDisposition = response.headers().get(HttpHeaders.CONTENT_DISPOSITION)
							.stream().findFirst();
					String originalFileName = headerDisposition.isPresent()
							? getFromHeader("filename", headerDisposition.get())
							: "filename";
					String name = headerDisposition.isPresent() ? getFromHeader("file", headerDisposition.get())
							: "file";

					byte[] payload = StreamUtils.copyToByteArray(response.body().asInputStream());
					return new InternalMultipartFile(name, originalFileName, contentType, payload);
				}
				return new SpringDecoder(messageConverters).decode(response, type);
			};
			return new ResponseEntityDecoder(decoder);
		}

		private String getFromHeader(String string, String headerValue) {
			Pattern regex = Pattern.compile(String.format("(?<=%s=\").*?(?=\")", string));
			Matcher regexMatcher = regex.matcher(headerValue);
			return regexMatcher.find() ? regexMatcher.group() : null;
		}
	}

	/**
	 * A dummy MultipartFile implementation
	 *
	 * @author resulav
	 */
	public class InternalMultipartFile implements MultipartFile {

		private final String name;
		private final String originalFileName;
		private final String contentType;
		private final byte[] payload;

		public InternalMultipartFile(String name, String originalFileName, String contentType, byte[] payload) {
			Preconditions.checkArgument(payload != null, "Payload cannot be null.");
			this.name = name;
			this.originalFileName = originalFileName;
			this.contentType = contentType;
			this.payload = payload;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getOriginalFilename() {
			return originalFileName;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public boolean isEmpty() {
			return payload.length == 0;
		}

		@Override
		public long getSize() {
			return payload.length;
		}

		@Override
		public byte[] getBytes() throws IOException {
			return payload;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(payload);
		}

		@SuppressWarnings("resource")
		@Override
		public void transferTo(File dest) throws IOException {
			try (OutputStream outputStream = new FileOutputStream(dest)) {
				outputStream.write(payload);
			}
		}
	}
}

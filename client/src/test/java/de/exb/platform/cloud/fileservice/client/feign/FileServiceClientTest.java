/**
 * 
 */
package de.exb.platform.cloud.fileservice.client.feign;

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.collect.Lists;
import com.netflix.loadbalancer.Server;

import de.exb.platform.cloud.fileservice.api.Constants;
import de.exb.platform.cloud.fileservice.client.feign.FileServiceClient.InternalMultipartFile;

/**
 * @author resulav
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "feign.hystrix.enabled=true" })
@ContextConfiguration(classes = { FileServiceClientTest.LocalRibbonClientConfiguration.class })
@EnableFeignClients(clients = FileServiceClient.class)
public class FileServiceClientTest {

	@ClassRule
	public static WireMockClassRule wiremock = new WireMockClassRule(wireMockConfig().dynamicPort());
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private FileServiceClient fileServiceClient;

	@Test
	public void testListFiles() throws JsonProcessingException {
		stubFor(get(urlEqualTo(String.format("%s/%s", Constants.API_FILECONTROLLER, "test")))
				.willReturn(ok(MAPPER.writeValueAsString(Lists.newArrayList("test.out")))
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
		List<String> fileList = fileServiceClient.listFiles("test");
		System.out.println(fileList);
		System.out.println(fileList.size());
	}

	@Test
	public void testDeleteFile() throws JsonProcessingException {
		stubFor(delete(urlEqualTo(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, "test", false)))
				.willReturn(ok().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
		fileServiceClient.deleteFile("test", false);
	}

	@Test
	public void testDownloadFile() throws JsonProcessingException {
		stubFor(get(urlEqualTo(
				String.format("%s/%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_DOWNLOAD, "test.txt")))
						.willReturn(ok("Spring Framework").withBody("Spring Framework")
								.withHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_DISPOSITION)
								.withHeader(HttpHeaders.CONTENT_DISPOSITION,
										String.format("attachment; filename=\"%s\"", "test.txt"))));

		fileServiceClient.downloadFile("test.txt");
	}

	@TestConfiguration
	public static class LocalRibbonClientConfiguration {

		@Bean
		public StaticServerList<Server> ribbonServerList() {
			return new StaticServerList<Server>(new Server("localhost", wiremock.port()));
		}
	}

	@Test
	public void testMultiPartFile() throws IOException {
		InternalMultipartFile multipartFile = new InternalMultipartFile("file", "file.txt", MediaType.TEXT_XML_VALUE,
				"InternalMultipartFile content".getBytes());

		Assert.assertNotNull("multipartFile can not be null", multipartFile);
		Assert.assertNotNull("multipartFile.name can not be null", multipartFile.getName());
		Assert.assertNotNull("multipartFile.originalFilename can not be null", multipartFile.getOriginalFilename());
		Assert.assertNotNull("multipartFile.contentType can not be null", multipartFile.getContentType());
		Assert.assertFalse("multipartFile is empty", multipartFile.isEmpty());
		Assert.assertTrue("multipartFile can not be null", multipartFile.getSize() > 0);
		Assert.assertNotNull("multipartFile.bytes can not be null", multipartFile.getBytes());
		Assert.assertNotNull("multipartFile.inputStream can not be null", multipartFile.getInputStream());

		Path path = Paths.get("transferToFile.txt");
		multipartFile.transferTo(path.toFile());
		if (Files.exists(path)) {
			Files.delete(path);
		}

	}

}

package de.exb.platform.cloud.fileservice.client.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.URLEncoder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import de.exb.platform.cloud.fileservice.api.Constants;
import de.exb.platform.cloud.fileservice.client.feign.FileServiceClient;

/**
 * @author resulav
 *
 */
@RunWith(SpringRunner.class)
public class FileServiceClientControllerTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Mock
	private FileServiceClient fileServiceClient;

	@InjectMocks
	private FileServiceClientController fileServiceClientController;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(fileServiceClientController).build();
	}

	@Test
	public void testList() throws Exception {
		Mockito.when(fileServiceClient.listFiles(Mockito.eq("test"))).thenReturn(Lists.newArrayList("sample.txt"));

		MvcResult result = mockMvc.perform(get(String.format("%s/%s", Constants.API_CLIENT_FILECONTROLLER, "test"))
				.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();

		String[] asArray = toStringArray(result.getResponse().getContentAsString());
		Assert.assertNotNull("result is null", asArray);
		Assert.assertEquals("Length mismatched", 1, asArray.length);
		Assert.assertEquals("Length mismatched", "sample.txt", asArray[0]);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testUpload() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain",
				"Spring Framework".getBytes());
		Mockito.doNothing().when(fileServiceClient).uploadFile(Mockito.eq(multipartFile));

		mockMvc.perform(
				fileUpload(String.format("%s/%s", Constants.API_CLIENT_FILECONTROLLER, Constants.API_METHOD_UPLOAD))
						.file(multipartFile).contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void testDownload() throws Exception {
		MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain",
				"Spring Framework".getBytes());
		Mockito.when(fileServiceClient.downloadFile(Mockito.eq("test"))).thenReturn(multipartFile);

		mockMvc.perform(
				get(String.format("%s/%s/%s", Constants.API_CLIENT_FILECONTROLLER, Constants.API_METHOD_DOWNLOAD,
						URLEncoder.encode("test", "UTF-8"))).contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void testDelete() throws Exception {
		Mockito.doNothing().when(fileServiceClient).deleteFile("test", false);
		mockMvc.perform(delete(String.format("%s/%s/%s", Constants.API_CLIENT_FILECONTROLLER, "test", "false"))
				.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());
	}

	/**
	 * @param contentAsString
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static String[] toStringArray(String contentAsString)
			throws JsonParseException, JsonMappingException, IOException {
		return MAPPER.readValue(contentAsString, String[].class);
	}

}

/**
 * 
 */
package de.exb.platform.cloud.fileservice.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.net.URLEncoder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
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

import com.google.common.collect.Lists;

import de.exb.platform.cloud.fileservice.TestUtility;
import de.exb.platform.cloud.fileservice.api.Constants;
import de.exb.platform.cloud.fileservice.service.FileService;
import de.exb.platform.cloud.fileservice.service.FileServiceException;

/**
 * @author resulav
 *
 */
@RunWith(SpringRunner.class)
public class FileServiceControllerTest {
	private MockMvc mockMvc;

	@Mock
	private FileService fileService;

	@InjectMocks
	private FileServiceController fileServiceController;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(fileServiceController)
				.setControllerAdvice(InternalExceptionHandler.class).build();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testUpload() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file", TestUtility.FILE_UPLOAD, "text/plain",
				"Spring Framework".getBytes());

		this.mockMvc.perform(fileUpload(String.format("%s/%s", Constants.API_FILECONTROLLER,Constants.API_METHOD_UPLOAD)).file(multipartFile))
				.andExpect(status().isOk());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testUploadConflict() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file", TestUtility.FILE_UPLOAD, "text/plain",
				"Spring Framework".getBytes());

		Mockito.doThrow(new FileServiceException("Dummy FileServiceException")).when(fileService)
				.store(Mockito.anyString(), Mockito.eq(multipartFile));

		this.mockMvc.perform(fileUpload(String.format("%s/%s", Constants.API_FILECONTROLLER,Constants.API_METHOD_UPLOAD)).file(multipartFile))
				.andExpect(status().isConflict());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testUploadBadRequest() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file", TestUtility.FILE_UPLOAD, "text/plain",
				"Spring Framework".getBytes());

		Mockito.doThrow(new IllegalArgumentException("Dummy IllegalArgumentException")).when(fileService)
				.store(Mockito.anyString(), Mockito.eq(multipartFile));

		this.mockMvc.perform(fileUpload(String.format("%s/%s", Constants.API_FILECONTROLLER,Constants.API_METHOD_UPLOAD)).file(multipartFile))
				.andExpect(status().isBadRequest());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testUploadInternalError() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file", TestUtility.FILE_UPLOAD, "text/plain",
				"Spring Framework".getBytes());

		Mockito.doThrow(new RuntimeException("Dummy exception")).when(fileService).store(Mockito.anyString(),
				Mockito.eq(multipartFile));

		this.mockMvc.perform(fileUpload(String.format("%s/%s", Constants.API_FILECONTROLLER,Constants.API_METHOD_UPLOAD)).file(multipartFile))
				.andExpect(status().isInternalServerError());
	}

	@Test
	public void testList() throws Exception {
		Mockito.when(fileService.listFiles(Mockito.any(String.class), Mockito.eq(TestUtility.TEST_FOLDER)))
				.thenReturn(Lists.newArrayList(TestUtility.TEST_FILE).stream());

		MvcResult result = mockMvc
				.perform(get(String.format("%s/%s", Constants.API_FILECONTROLLER, TestUtility.TEST_FOLDER))
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk()).andReturn();

		String[] asArray = TestUtility.toStringArray(result.getResponse().getContentAsString());
		Assert.assertNotNull("result is null", asArray);
		Assert.assertEquals("Length mismatched", 1, asArray.length);
		Assert.assertEquals("Length mismatched", TestUtility.TEST_FILE, asArray[0]);
	}

	@Test
	public void testListBadRequest() throws Exception {
		Mockito.when(fileService.listFiles(Mockito.any(String.class), ArgumentMatchers.eq(TestUtility.TEST_FOLDER)))
				.thenThrow(IllegalArgumentException.class);
		mockMvc.perform(get(String.format("%s/%s", Constants.API_FILECONTROLLER, TestUtility.TEST_FOLDER))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testListConflict() throws Exception {
		Mockito.when(fileService.listFiles(Mockito.any(String.class), ArgumentMatchers.eq(TestUtility.TEST_FOLDER)))
				.thenThrow(FileServiceException.class);
		mockMvc.perform(get(String.format("%s/%s", Constants.API_FILECONTROLLER, TestUtility.TEST_FOLDER))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isConflict());
	}

	@Test
	public void testListInternalError() throws Exception {
		Mockito.when(fileService.listFiles(Mockito.any(String.class), ArgumentMatchers.eq(TestUtility.TEST_FOLDER)))
				.thenThrow(RuntimeException.class);
		mockMvc.perform(get(String.format("%s/%s", Constants.API_FILECONTROLLER, TestUtility.TEST_FOLDER))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testDelete() throws Exception {
		mockMvc.perform(delete(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, TestUtility.TEST_FILE, "false"))
				.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void testDeleteBadRequest() throws Exception {
		Mockito.doThrow(new IllegalArgumentException("Dummy exception")).when(fileService)
				.delete(Mockito.any(String.class), ArgumentMatchers.eq(TestUtility.TEST_FILE), Mockito.eq(false));

		mockMvc.perform(delete(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, TestUtility.TEST_FILE, "false"))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testDeleteConflict() throws Exception {
		Mockito.doThrow(new FileServiceException("Dummy exception")).when(fileService).delete(Mockito.any(String.class),
				ArgumentMatchers.eq(TestUtility.TEST_FILE), Mockito.eq(false));

		mockMvc.perform(delete(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, TestUtility.TEST_FILE, "false"))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isConflict());
	}

	@Test
	public void testDeleteInternalError() throws Exception {
		Mockito.doThrow(new RuntimeException("Dummy exception")).when(fileService).delete(Mockito.any(String.class),
				ArgumentMatchers.eq(TestUtility.TEST_FILE), Mockito.eq(false));

		mockMvc.perform(delete(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, TestUtility.TEST_FILE, "false"))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError());
	}

	// read CASEs
	@Test
	public void testRead() throws Exception {
		Mockito.when(fileService.openForReading(Mockito.any(String.class), ArgumentMatchers.eq(TestUtility.EXIST_FILE)))
				.thenReturn(new ByteArrayInputStream("some content".getBytes()));
		mockMvc.perform(get(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_DOWNLOAD,
				URLEncoder.encode(TestUtility.EXIST_FILE, "UTF-8"))).contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void testReadBadRequest() throws Exception {
		Mockito.doThrow(new IllegalArgumentException("Dummy exception")).when(fileService)
				.delete(Mockito.any(String.class), ArgumentMatchers.eq(TestUtility.TEST_FILE), Mockito.eq(false));
		mockMvc.perform(delete(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_DOWNLOAD,
				URLEncoder.encode("../test.txt", "UTF-8"))).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testReadConflict() throws Exception {
		Mockito.when(fileService.openForReading(Mockito.any(String.class), ArgumentMatchers.eq(TestUtility.EXIST_FILE)))
				.thenThrow(FileServiceException.class);

		mockMvc.perform(get(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_DOWNLOAD,
				URLEncoder.encode(TestUtility.EXIST_FILE, "UTF-8"))).contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isConflict());
	}

	@Test
	public void testReadInternalError() throws Exception {
		Mockito.when(fileService.openForReading(Mockito.any(String.class), ArgumentMatchers.eq(TestUtility.EXIST_FILE)))
				.thenThrow(RuntimeException.class);

		mockMvc.perform(get(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_DOWNLOAD,
				URLEncoder.encode(TestUtility.EXIST_FILE, "UTF-8"))).contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isInternalServerError());
	}

}

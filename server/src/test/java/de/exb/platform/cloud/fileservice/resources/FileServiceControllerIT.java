/**
 * 
 */
package de.exb.platform.cloud.fileservice.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.exb.platform.cloud.fileservice.TestUtility;
import de.exb.platform.cloud.fileservice.api.Constants;

/**
 * @author resulav
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileServiceControllerIT {

	@Autowired
	private MockMvc mvc;

	@BeforeClass
	public static void setup() throws IOException {
		TestUtility.createUploadStructure();
	}

	@AfterClass
	public static void teardown() throws IOException {
		TestUtility.deleteUploadFolder();
	}

	/**
	 * This test creates a folder and a file under the folder for some test cases.
	 * The test is different from @Before, because we want to run this step one time
	 * in a test execution duration.
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void firstTest() throws Exception {
		MvcResult result = mvc.perform(
				get(String.format("%s/%s", Constants.API_FILECONTROLLER, ".")).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		List<String> list = TestUtility.toStringList(result.getResponse().getContentAsString());
		Assert.assertNotNull("result is null", list);

		if (list.contains(TestUtility.EXIST_FILE)) {
			return;
		}

		MockMultipartFile multipartFile = new MockMultipartFile("file", TestUtility.EXIST_FILE, "text/plain",
				"Spring Framework".getBytes());

		this.mvc.perform(fileUpload(String.format("%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_UPLOAD))
				.file(multipartFile)).andExpect(status().isOk());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testUploadListAndDelete() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file", TestUtility.FILE_UPLOAD, "text/plain",
				"Spring Framework".getBytes());

		this.mvc.perform(fileUpload(String.format("%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_UPLOAD))
				.file(multipartFile)).andExpect(status().isOk());

		MvcResult result = mvc.perform(
				get(String.format("%s/%s", Constants.API_FILECONTROLLER, ".")).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		List<String> list = TestUtility.toStringList(result.getResponse().getContentAsString());
		Assert.assertNotNull("result is null", list);
		Assert.assertTrue("result is empty", list.contains(TestUtility.FILE_UPLOAD));

		this.mvc.perform(
				delete(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, TestUtility.FILE_UPLOAD, "false"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testUploadListAndDeleteRecursive() throws Exception {

		MockMultipartFile multipartFile = new MockMultipartFile("file",
				String.format("%s/%s", TestUtility.TEST_FOLDER, TestUtility.FILE_UPLOAD), "text/plain",
				"Spring Framework".getBytes());

		this.mvc.perform(fileUpload(String.format("%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_UPLOAD))
				.file(multipartFile)).andExpect(status().isOk());

		MvcResult result = mvc
				.perform(get(String.format("%s/%s", Constants.API_FILECONTROLLER, TestUtility.TEST_FOLDER))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();

		List<String> list = TestUtility.toStringList(result.getResponse().getContentAsString());
		Assert.assertNotNull("result is null", list);
		Assert.assertTrue("result is empty",
				list.contains(String.format("%s/%s", TestUtility.TEST_FOLDER, TestUtility.FILE_UPLOAD)));

		this.mvc.perform(delete(String.format("%s/%s/%s", Constants.API_FILECONTROLLER,
				URLEncoder.encode(String.format("%s/%s", TestUtility.TEST_FOLDER, TestUtility.FILE_UPLOAD), "UTF-8"),
				"true")).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void testDownload() throws Exception {
		MvcResult result = mvc
				.perform(get(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_DOWNLOAD,
						URLEncoder.encode(TestUtility.EXIST_FILE, "UTF-8"))).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();
		Assert.assertNotNull("no CONTENT_DISPOSITION header",
				result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
	}

	@Test
	public void testDownloadNoFile() throws Exception {
		mvc.perform(get(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_DOWNLOAD,
				URLEncoder.encode("nofile.txt", "UTF-8"))).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isConflict());
	}

	@Test
	public void testListBadRequest() throws Exception {
		mvc.perform(get(String.format("%s/%s", Constants.API_FILECONTROLLER, URLEncoder.encode(".../test", "UTF-8")))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void testDeleteBadRequest() throws Exception {
		this.mvc.perform(delete(
				String.format("%s/%s/%s", Constants.API_FILECONTROLLER, URLEncoder.encode("..", "UTF-8"), "false"))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testUploadBadRequest() throws Exception {

		MockMultipartFile multipartFile = new MockMultipartFile("file", "../test.txt", "text/plain",
				"Spring Framework".getBytes());

		this.mvc.perform(fileUpload(String.format("%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_UPLOAD))
				.file(multipartFile)).andExpect(status().isBadRequest());
	}

	@Test
	public void testReadBadRequest() throws Exception {
		mvc.perform(get(String.format("%s/%s/%s", Constants.API_FILECONTROLLER, Constants.API_METHOD_DOWNLOAD,
				URLEncoder.encode("../test.txt", "UTF-8"))).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

}

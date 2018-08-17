/**
 * 
 */
package de.exb.platform.cloud.fileservice.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import de.exb.platform.cloud.fileservice.TestUtility;
import de.exb.platform.cloud.fileservice.properties.StorageProperties;

/**
 * @author resulav
 *
 */
@RunWith(SpringRunner.class)
@EnableConfigurationProperties(StorageProperties.class)
@SpringBootTest
public class FileServiceImplTest {

	@Autowired
	private FileServiceImpl fileService;

	private final String sessionId = "dummySessionId";

	@Autowired
	public StorageProperties properties;

	@BeforeClass
	public static void setup() throws IOException {
		TestUtility.createUploadStructure();
	}

	@AfterClass
	public static void teardown() throws IOException {
		TestUtility.deleteUploadFolder();
	}

	// list CASES
	@Test
	public void testList() throws FileServiceException {
		Stream<Path> stream = fileService.list(sessionId, TestUtility.TEST_FOLDER);
		Assert.assertTrue("stream can not be null", stream != null);
		Assert.assertTrue("stream empty", stream.count() > 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testListNullPath() throws FileServiceException {
		fileService.list(sessionId, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testListEmptyPath() throws FileServiceException {
		fileService.list(sessionId, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testListOutOfPath() throws FileServiceException {
		fileService.list(sessionId, "../");
	}

	@Test
	public void testListNotExist() throws FileServiceException {
		Stream<Path> stream = fileService.list(sessionId, "notExist");
		Assert.assertTrue("stream can not be null", stream != null);
		Assert.assertTrue("stream empty", stream.count() == 0);
	}

	// listFiles CASES
	@Test
	public void testListFiles() throws FileServiceException {
		Stream<String> stream = fileService.listFiles(sessionId, TestUtility.TEST_FOLDER);
		Assert.assertTrue("stream can not be null", stream != null);
		Assert.assertTrue("stream empty", stream.count() > 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testListFilesNullPath() throws FileServiceException {
		fileService.listFiles(sessionId, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testListFilesEmptyPath() throws FileServiceException {
		fileService.listFiles(sessionId, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testListFilesOutOfPath() throws FileServiceException {
		fileService.listFiles(sessionId, "../");
	}

	@Test
	public void testListFilesNotExist() throws FileServiceException {
		Stream<String> stream = fileService.listFiles(sessionId, "notExist");
		Assert.assertTrue("stream can not be null", stream != null);
		Assert.assertTrue("stream empty", stream.count() == 0);
	}

	// getLength CASES
	@Test
	public void testGetLength() throws FileServiceException {
		long length = fileService.getLength(TestUtility.TEST_FOLDER);
		Assert.assertTrue("length should be positive", length > 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetLengthNullPath() throws FileServiceException {
		fileService.getLength(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetLengthEmptyPath() throws FileServiceException {
		fileService.getLength("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetLengthOutOfPath() throws FileServiceException {
		fileService.getLength("../");
	}

	@Test(expected = FileServiceException.class)
	public void testGetLengthNotExist() throws FileServiceException {
		fileService.getLength("notExist");
	}

	// store CASES
	@Test
	public void testStore() throws FileServiceException {
		MockMultipartFile multipartFile = new MockMultipartFile("file", TestUtility.FILE_UPLOAD, "text/plain",
				"Spring Framework".getBytes());
		fileService.store(sessionId, multipartFile);
		Assert.assertTrue("store unsuccessful", fileService.exists(sessionId, TestUtility.FILE_UPLOAD));
		fileService.delete(sessionId, TestUtility.FILE_UPLOAD, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStoreEmptyPath() throws FileServiceException {
		MockMultipartFile multipartFile = new MockMultipartFile("file", "", "text/plain",
				"Spring Framework".getBytes());
		fileService.store(sessionId, multipartFile);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStoreNull() throws FileServiceException {
		fileService.store(sessionId, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStoreNullPath() throws FileServiceException {
		MockMultipartFile multipartFile = new MockMultipartFile("file", null, "text/plain",
				"Spring Framework".getBytes());
		fileService.store(sessionId, multipartFile);
	}

	@Test(expected = FileServiceException.class)
	public void testStoreAlreadyExist() throws FileServiceException {
		MockMultipartFile multipartFile = new MockMultipartFile("file", TestUtility.EXIST_FILE, "text/plain",
				"Spring Framework".getBytes());
		fileService.store(sessionId, multipartFile);
	}

	// exist CASES
	@Test
	public void testExist() throws FileServiceException {
		Assert.assertTrue("file not exist", fileService.exists(sessionId, TestUtility.EXIST_FILE));
	}

	@Test
	public void testExistNot() throws FileServiceException {
		Assert.assertFalse("store unsuccessful", fileService.exists(sessionId, "notExist.txt"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExistNullPath() throws FileServiceException {
		fileService.exists(sessionId, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExistEmptyPath() throws FileServiceException {
		fileService.exists(sessionId, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExistOutOfPath() throws FileServiceException {
		fileService.exists(sessionId, "../");
	}

	// delete CASES
	@Test
	public void testDelete() throws FileServiceException {
		MockMultipartFile multipartFile = new MockMultipartFile("file", "dummy/delete.txt", "text/plain",
				"Spring Framework".getBytes());
		fileService.store(sessionId, multipartFile);
		fileService.delete(sessionId, "dummy/delete.txt", true);
	}

	@Test(expected = FileServiceException.class)
	public void testDeleteNotExist() throws FileServiceException {
		fileService.delete(sessionId, "notExist.txt", false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteNullPath() throws FileServiceException {
		fileService.delete(sessionId, null, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteEmptyPath() throws FileServiceException {
		fileService.delete(sessionId, "", false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteOutOfPath() throws FileServiceException {
		fileService.delete(sessionId, "../", false);
	}

	// getParent CASES
	@Test
	public void testGetParent() throws FileServiceException {
		String parent = fileService.getParent(sessionId, "test");
		Assert.assertNotNull("Parent is null", parent);
	}

	@Test(expected = FileServiceException.class)
	public void testGetParentNotExist() throws FileServiceException {
		String parent = fileService.getParent(sessionId, "notExist.txt");
		Assert.assertNull("Parent is not null", parent);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetParentNullPath() throws FileServiceException {
		fileService.getParent(sessionId, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetParentEmptyPath() throws FileServiceException {
		fileService.getParent(sessionId, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetParentOutOfPath() throws FileServiceException {
		fileService.getParent(sessionId, "..");
	}

	// openForWriting CASES
	@Test
	public void testOpenForWriting() throws FileServiceException {
		OutputStream stream = fileService.openForWriting(sessionId, TestUtility.EXIST_FILE, false);
		Assert.assertNotNull("Parent is null", stream);
	}

	@Test(expected = FileServiceException.class)
	public void testOpenForWritingNotExist() throws FileServiceException {
		fileService.openForWriting(sessionId, "notExist.txt", false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenForWritingNullPath() throws FileServiceException {
		fileService.openForWriting(sessionId, null, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenForWritingEmptyPath() throws FileServiceException {
		fileService.openForWriting(sessionId, "", false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenForWritingOutOfPath() throws FileServiceException {
		fileService.openForWriting(sessionId, "..", false);
	}

	// openForReading CASES
	@Test
	public void testOpenForReading() throws FileServiceException {
		InputStream stream = fileService.openForReading(sessionId, TestUtility.EXIST_FILE);
		Assert.assertNotNull("Parent is null", stream);
	}

	@Test(expected = FileServiceException.class)
	public void testOpenForReadingNotExist() throws FileServiceException {
		fileService.openForReading(sessionId, "notExist.txt");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenForReadingNullPath() throws FileServiceException {
		fileService.openForReading(sessionId, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenForReadingEmptyPath() throws FileServiceException {
		fileService.openForReading(sessionId, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenForReadingOutOfPath() throws FileServiceException {
		fileService.openForReading(sessionId, "..");
	}

}

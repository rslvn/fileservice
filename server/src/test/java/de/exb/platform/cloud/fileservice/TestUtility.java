/**
 * 
 */
package de.exb.platform.cloud.fileservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author resulav
 *
 */
public class TestUtility {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final TypeFactory TYPE_FACTORY = MAPPER.getTypeFactory();

	public static final Path UPLOAD_FOLDER = Paths.get("uploads-test");

	public static final String TEST_FOLDER = "test";
	public static final String TEST_FILE = "sample.txt";
	public static final String FILE_UPLOAD = "upload.txt";
	public static final String FILE_DOWNLOAD = "download.txt";
	public static final String EXIST_FILE = TEST_FOLDER + "/" + TEST_FILE;

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

	/**
	 * @param contentAsString
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static List<String> toStringList(String contentAsString)
			throws JsonParseException, JsonMappingException, IOException {
		return MAPPER.readValue(contentAsString, TYPE_FACTORY.constructCollectionType(List.class, String.class));
	}

	/**
	 * delete test uploads folder
	 * 
	 * @throws IOException
	 */
	public static void deleteUploadFolder() throws IOException {
		if (!UPLOAD_FOLDER.toFile().exists()) {
			return;
		}
		try (Stream<Path> stream = Files.walk(UPLOAD_FOLDER)) {
			stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}
		Files.deleteIfExists(UPLOAD_FOLDER);
	}

	/**
	 * create test uploads folder
	 * 
	 * @throws IOException
	 */
	public static void createUploadStructure() throws IOException {
		if (!UPLOAD_FOLDER.toFile().exists()) {
			Files.createDirectories(UPLOAD_FOLDER);
		}

		Path path = UPLOAD_FOLDER.resolve(TEST_FOLDER);
		if (!path.toFile().exists()) {
			Files.createDirectories(path);
		}

		path = UPLOAD_FOLDER.resolve(EXIST_FILE);
		if (!path.toFile().exists()) {
			Files.createFile(path);
		}

	}

}

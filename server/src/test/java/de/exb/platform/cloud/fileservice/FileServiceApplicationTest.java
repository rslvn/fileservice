/**
 * 
 */
package de.exb.platform.cloud.fileservice;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.exb.platform.cloud.fileservice.service.FileService;

/**
 * @author resulav
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FileServiceApplicationTest {
	@Autowired
	private FileService fileService;

	@Test
	public void contextLoads() {
		Assert.assertNotNull("fileService can not be null", fileService);
	}
}

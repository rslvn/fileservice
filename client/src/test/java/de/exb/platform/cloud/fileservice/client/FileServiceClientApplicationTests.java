package de.exb.platform.cloud.fileservice.client;

import de.exb.platform.cloud.fileservice.client.feign.FileServiceClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = FileServiceClientApplication.class)
public class FileServiceClientApplicationTests {

	@Autowired
	private FileServiceClient fileServiceClient;

	@Test
	public void contextLoads() {
		Assert.assertNotNull("fileServiceClient can not be null", fileServiceClient);
	}
}

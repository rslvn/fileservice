/**
 * 
 */
package de.exb.platform.cloud.fileservice.service;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author resulav
 *
 */
public class FileServiceExceptionTest {

	@Test(expected = FileServiceException.class)
	public void testExceptionWithoutCause() throws FileServiceException {
		FileServiceException e = new FileServiceException("dummy exception");
		Assert.assertNull("Cause not null", e.getCause());
		throw e;
	}

	@Test(expected = FileServiceException.class)
	public void testExceptionWithCause() throws FileServiceException {
		FileServiceException e = new FileServiceException("dummy exception",
				new RuntimeException("dummy runtime exception"));
		Assert.assertNotNull("Cause is null", e.getCause());
		throw e;
	}

}

/**
 * 
 */
package de.exb.platform.cloud.fileservice.api;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author resulav
 *
 */
public class ConstantsTests {

	@Test
	public void testConstructor() {
		Constants constants = new Constants();
		assertNotNull("constants can not be null", constants);
	}
}

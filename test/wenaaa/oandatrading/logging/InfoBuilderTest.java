package wenaaa.oandatrading.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InfoBuilderTest {

	@Test
	public void testStartLength() {
		final InfoBuilder s1 = new InfoBuilder("1");
		final InfoBuilder s2 = new InfoBuilder("12345678901234567890");
		final InfoBuilder s3 = new InfoBuilder("123456789012345678901234567890");
		assertEquals(21, s1.toString().length());
		assertEquals(21, s2.toString().length());
		assertEquals(31, s3.toString().length());
	}

	@Test
	public void testBuildingInfo() {
		final InfoBuilder s2 = new InfoBuilder("123456789012345678");
		assertTrue("123456789012345678  :".equalsIgnoreCase(s2.toString()));
		s2.append(32, false);
		assertEquals("123456789012345678  : 32 /", s2.toString());
		s2.append(true, false);
		assertEquals("123456789012345678  : 32 / true /", s2.toString());
		s2.append("end", true);
		assertEquals("123456789012345678  : 32 / true / end", s2.toString());
	}
}

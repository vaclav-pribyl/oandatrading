package wenaaa.oandatrading.properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.InvalidParameterException;

import org.junit.Test;

public class TestTradedPair {

	@Test(expected = InvalidParameterException.class)
	public void testException() {
		new TradedPair("AUD/USD", "position");
	}

	@Test
	public void testIsBuyPair() {
		final TradedPair b = new TradedPair("name", "lOng");
		assertTrue(b.isBuyPair());
		final TradedPair s = new TradedPair("name", "Short");
		assertFalse(s.isBuyPair());
	}
}

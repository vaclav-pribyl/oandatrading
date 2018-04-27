package wenaaa.oandatrading.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StopOrderTest {
	@Test
	public void testPriceString() {
		final MockStopOrder lo1 = new MockStopOrder(0.12345678, 5);
		assertEquals("0.12345", lo1.getPriceString());
		final MockStopOrder lo2 = new MockStopOrder(0.12345678, 3);
		assertEquals("0.123", lo2.getPriceString());
		final MockStopOrder lo3 = new MockStopOrder(0.1, 6);
		assertEquals("0.1", lo3.getPriceString());
	}

	private static class MockStopOrder extends StopOrder {

		private final int dp;

		public MockStopOrder(final double d, final int i) {
			setPrice(d);
			dp = i;
		}

		@Override
		int getDisplayPrecission(final String pair) {
			return dp;
		}
	}
}

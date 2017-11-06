package wenaaa.oandatrading;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.oanda.fxtrade.api.AccountException;

public class TestTrader {

	@Test
	public void testTrade() throws AccountException, InterruptedException {
		final Trader trader = mock(Trader.class);
		doCallRealMethod().when(trader).trade();
		final long past = System.currentTimeMillis() - 1000;
		final long future = past + 2000;
		when(trader.getReportTime()).thenReturn(past).thenReturn(future);
		for (int i = 0; i < 122; i++) {
			trader.trade();
		}
		verify(trader, times(120)).handleSL();
		verify(trader, times(120)).postOrders();
		verify(trader, times(122)).sleep(1000);
		verify(trader, times(2)).printInfo();
		verify(trader, times(2)).lastBalanceReset();
		verify(trader, times(2)).getReportTime();
		verify(trader, times(1)).report();
		verify(trader, times(1)).resetReportTime();
	}
}

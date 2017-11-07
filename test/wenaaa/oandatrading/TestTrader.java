package wenaaa.oandatrading;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.oanda.fxtrade.api.Account;
import com.oanda.fxtrade.api.AccountException;

import wenaaa.oandatrading.properties.PropertyManager;

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

	@Test
	public void testLastBalanceReset() throws AccountException {
		final Trader trader = mock(Trader.class);
		doCallRealMethod().when(trader).lastBalanceReset();
		PropertyManager.setResetBalanceRatio(2);
		when(trader.getLastBalance()).thenReturn(200.0);
		when(trader.getTotalBalance()).thenReturn(399.9).thenReturn(400.1);
		trader.lastBalanceReset();
		verify(trader, times(1)).getLastBalance();
		verify(trader, times(1)).getTotalBalance();
		verify(trader, never()).closeTrades();
		verify(trader, never()).updateLastBalance();
		trader.lastBalanceReset();
		verify(trader, times(2)).getLastBalance();
		verify(trader, times(2)).getTotalBalance();
		verify(trader, times(1)).closeTrades();
		verify(trader, times(1)).updateLastBalance();
	}

	@Test
	public void testUpdateLastBalance() throws AccountException {
		final Trader trader = mock(Trader.class);
		doCallRealMethod().when(trader).updateLastBalance();
		when(trader.getLastBalance()).thenCallRealMethod();
		when(trader.getTotalBalance()).thenReturn(123.456);
		trader.updateLastBalance();
		verify(trader, times(1)).storeLastBalance();
		assertEquals(123.456, trader.getLastBalance(), 1e-6);
	}

	@Test
	public void testGetTotalBalance() throws AccountException {
		final Trader trader = mock(Trader.class);
		when(trader.getTotalBalance()).thenCallRealMethod();
		final Collection<Account> accs = new ArrayList<>(3);
		when(trader.getAccounts()).thenReturn(accs);
		final Account acc1 = mock(Account.class);
		final Account acc2 = mock(Account.class);
		final Account acc3 = mock(Account.class);
		when(acc1.getBalance()).thenReturn(1.2);
		when(acc2.getBalance()).thenReturn(2.3);
		when(acc3.getBalance()).thenReturn(3.4);
		accs.add(acc1);
		accs.add(acc2);
		accs.add(acc3);
		assertEquals(6.9, trader.getTotalBalance(), 1e-6);
	}
}

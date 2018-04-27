package wenaaa.oandatrading;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.security.auth.login.AccountException;

import org.junit.Test;

import wenaaa.oandatrading.api.Account;
import wenaaa.oandatrading.properties.PropertyManager;

public class TestTrader {

	@Test
	public void testTrade() throws InterruptedException {
		final Trader trader = mock(Trader.class);
		doCallRealMethod().when(trader).trade();
		final LocalDateTime past = LocalDateTime.now().minusSeconds(10);
		final LocalDateTime future = past.plusSeconds(20);
		when(trader.getReportTime()).thenReturn(past).thenReturn(future);
		for (int i = 0; i < 122; i++) {
			trader.trade();
		}
		verify(trader, times(120)).handleSL();
		verify(trader, times(120)).postOrders();
		verify(trader, times(122)).sleep(1000);
		verify(trader, times(120)).printInfo();
		verify(trader, times(2)).lastBalanceReset();
		verify(trader, times(2)).getReportTime();
		verify(trader, times(1)).report();
		verify(trader, times(1)).resetReportTime();
	}

	@Test
	public void testLastBalanceReset() {
		final Trader trader = mock(Trader.class);
		doCallRealMethod().when(trader).lastBalanceReset();
		PropertyManager.setResetBalanceRatio(2);
		when(trader.getLastBalance()).thenReturn(200.0);
		when(trader.getTotalBalance()).thenReturn(399.9).thenReturn(401.1).thenReturn(405.2);
		when(trader.getTotalUPL()).thenReturn(-1.0).thenReturn(-1.5).thenReturn(-5.0);
		when(trader.getBalanceReset()).thenCallRealMethod();
		trader.lastBalanceReset();
		verify(trader, times(1)).getLastBalance();
		verify(trader, times(1)).getTotalBalance();
		verify(trader, never()).closeTrades();
		verify(trader, never()).updateLastBalance();
		trader.lastBalanceReset();
		verify(trader, times(2)).getLastBalance();
		verify(trader, times(2)).getTotalBalance();
		verify(trader, never()).closeTrades();
		verify(trader, never()).updateLastBalance();
		trader.lastBalanceReset();
		verify(trader, times(3)).getLastBalance();
		verify(trader, times(3)).getTotalBalance();
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

	@Test
	public void testGetTotalUPL() throws AccountException {
		final Trader trader = mock(Trader.class);
		when(trader.getTotalUPL()).thenCallRealMethod();
		final Collection<Account> accs = new ArrayList<>(3);
		when(trader.getAccounts()).thenReturn(accs);
		final Account acc1 = mock(Account.class);
		final Account acc2 = mock(Account.class);
		final Account acc3 = mock(Account.class);
		when(acc1.getUnrealizedPL()).thenReturn(1.2);
		when(acc2.getUnrealizedPL()).thenReturn(-2.3);
		when(acc3.getUnrealizedPL()).thenReturn(3.4);
		accs.add(acc1);
		accs.add(acc2);
		accs.add(acc3);
		assertEquals(2.3, trader.getTotalUPL(), 1e-6);
	}

	@Test
	public void testGetTotalOrders() throws AccountException {
		final Trader trader = mock(Trader.class);
		when(trader.getTotalOrders()).thenCallRealMethod();
		final Collection<Account> accs = new ArrayList<>(3);
		when(trader.getAccounts()).thenReturn(accs);
		final Account acc1 = mock(Account.class);
		final Account acc2 = mock(Account.class);
		final Account acc3 = mock(Account.class);
		when(acc1.getOrdersCount()).thenReturn(12);
		when(acc2.getOrdersCount()).thenReturn(100);
		when(acc3.getOrdersCount()).thenReturn(56);
		accs.add(acc1);
		accs.add(acc2);
		accs.add(acc3);
		assertEquals(168, trader.getTotalOrders());
	}

	@Test
	public void testGetTotalTrades() throws AccountException {
		final Trader trader = mock(Trader.class);
		when(trader.getTotalTrades()).thenCallRealMethod();
		final Collection<Account> accs = new ArrayList<>(3);
		when(trader.getAccounts()).thenReturn(accs);
		final Account acc1 = mock(Account.class);
		final Account acc2 = mock(Account.class);
		final Account acc3 = mock(Account.class);
		final Vector v1 = mock(Vector.class);
		final Vector v2 = mock(Vector.class);
		final Vector v3 = mock(Vector.class);
		when(acc1.getTrades()).thenReturn(v1);
		when(acc2.getTrades()).thenReturn(v2);
		when(acc3.getTrades()).thenReturn(v3);
		when(v1.size()).thenReturn(120);
		when(v2.size()).thenReturn(100);
		when(v3.size()).thenReturn(156);
		accs.add(acc1);
		accs.add(acc2);
		accs.add(acc3);
		assertEquals(376, trader.getTotalTrades());
	}

	@Test
	public void testReportResults() throws AccountException {
		final Trader trader = mock(Trader.class);
		doCallRealMethod().when(trader).reportResults(any(PrintWriter.class));
		when(trader.getTotalBalance()).thenReturn(123.456);
		when(trader.getTotalUPL()).thenReturn(-20.0);
		when(trader.getTotalTrades()).thenReturn(54);
		when(trader.getTotalOrders()).thenReturn(23);
		final String date = LocalDate.now().toString();
		final PrintWriter pw = mock(PrintWriter.class);
		trader.reportResults(pw);
		verify(pw, times(1)).println(date + " 123.456 103.456 54 23");
	}

	@Test
	public void testResetReportTime() {
		final Trader trader = mock(Trader.class);
		doCallRealMethod().when(trader).resetReportTime();
		when(trader.getReportTime()).thenCallRealMethod();
		trader.resetReportTime();
		final LocalDateTime datetime = trader.getReportTime();
		final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).withHour(9);
		final Duration dur = Duration.between(now, datetime);
		assertEquals(24, dur.toHours());
	}

	@Test
	public void testInitReportTime() throws IOException {
		final Trader trader = mock(Trader.class);
		doCallRealMethod().when(trader).initReportTime(any(BufferedReader.class));
		when(trader.getReportTime()).thenCallRealMethod();
		final BufferedReader br = mock(BufferedReader.class);
		final String added = " 12.1 21.2 3 6";
		when(br.readLine()).thenReturn(LocalDate.now().minusDays(3).toString() + added)
				.thenReturn(LocalDate.now().minusDays(2).toString() + added)
				.thenReturn(LocalDate.now().minusDays(1).toString() + added).thenReturn(null);
		trader.initReportTime(br);
		verify(trader, never()).resetReportTime();
		assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).withHour(9), trader.getReportTime());
		when(br.readLine()).thenReturn(LocalDate.now().minusDays(2).toString() + added)
				.thenReturn(LocalDate.now().minusDays(1).toString() + added)
				.thenReturn(LocalDate.now().toString() + added).thenReturn(null);
		trader.initReportTime(br);
		verify(trader, times(1)).resetReportTime();
	}
}

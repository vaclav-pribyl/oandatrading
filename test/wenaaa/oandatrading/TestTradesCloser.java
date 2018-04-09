package wenaaa.oandatrading;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.junit.Test;

import wenaaa.oandatrading.api.Account;
import wenaaa.oandatrading.api.MarketOrder;

public class TestTradesCloser {

	@Test
	public void testGetTimeStop() {
		final TradesCloser closer = mock(TradesCloser.class);
		when(closer.getTimeStop()).thenCallRealMethod();
		final long ref = System.currentTimeMillis() / 1000 - 60 * 60 * 24 * 10;
		assertEquals(ref, closer.getTimeStop());
	}

	@Test
	public void testCloseTrades() {
		final TradesCloser closer = mock(TradesCloser.class);
		doCallRealMethod().when(closer).closeTrades();
		final Collection<Account> accs = new ArrayList<Account>();
		final Account acc1 = mock(Account.class);
		final Account acc2 = mock(Account.class);
		final Account acc3 = mock(Account.class);
		accs.add(acc1);
		accs.add(acc2);
		accs.add(acc3);
		when(closer.getAccounts()).thenReturn(accs);
		closer.closeTrades();
		verify(closer, times(1)).closeAccountTrades(acc1);
		verify(closer, times(1)).closeAccountTrades(acc2);
		verify(closer, times(1)).closeAccountTrades(acc3);
	}

	@Test
	public void testCloseAccountTrades() {
		final TradesCloser closer = mock(TradesCloser.class);
		final Account acc = mock(Account.class);
		doCallRealMethod().when(closer).closeAccountTrades(acc);
		final Vector<MarketOrder> trades = new Vector<>();
		when(closer.getOpenTrades(acc)).thenReturn(trades);
		final MarketOrder t1 = mock(MarketOrder.class);// no sl one day old -3.3 upl
		when(closer.hasSL(t1)).thenReturn(false);
		when(t1.getTimestamp()).thenReturn(ZonedDateTime.now().minusDays(1).toEpochSecond());
		when(closer.getUPL(t1)).thenReturn(-3.3);
		trades.add(t1);
		final MarketOrder t2 = mock(MarketOrder.class);// has SL
		when(closer.hasSL(t2)).thenReturn(true);
		trades.add(t2);
		final MarketOrder t3 = mock(MarketOrder.class);// no sl 11 days old
		when(closer.hasSL(t3)).thenReturn(false);
		when(t3.getTimestamp()).thenReturn(ZonedDateTime.now().minusDays(11).toEpochSecond());
		trades.add(t3);
		final MarketOrder t4 = mock(MarketOrder.class);// no sl one day old -2.3 upl
		when(closer.hasSL(t4)).thenReturn(false);
		when(t4.getTimestamp()).thenReturn(ZonedDateTime.now().minusDays(1).toEpochSecond());
		when(closer.getUPL(t1)).thenReturn(-2.3);
		trades.add(t4);
		when(closer.getTimeStop()).thenCallRealMethod();
		when(closer.getPair(any(MarketOrder.class))).thenReturn("AUD/USD");
		closer.closeAccountTrades(acc);

		verify(acc, times(1)).close(t1);
		verify(acc, never()).close(t2);
		verify(acc, times(1)).close(t3);
		verify(acc, never()).close(t4);
		verify(t1, times(1)).getTimestamp();
		verify(t2, never()).getTimestamp();
		verify(t3, times(1)).getTimestamp();
		verify(t4, times(1)).getTimestamp();
		verify(closer, times(1)).getUPL(t1);
		verify(closer, never()).getUPL(t2);
		verify(closer, never()).getUPL(t3);
		verify(closer, times(1)).getUPL(t4);
	}

}

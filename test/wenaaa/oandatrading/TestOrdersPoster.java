package wenaaa.oandatrading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.junit.Test;

import com.oanda.fxtrade.api.Account;
import com.oanda.fxtrade.api.AccountException;
import com.oanda.fxtrade.api.FXPair;
import com.oanda.fxtrade.api.FXTick;
import com.oanda.fxtrade.api.LimitOrder;
import com.oanda.fxtrade.api.MarketOrder;
import com.oanda.fxtrade.api.OAException;
import com.oanda.fxtrade.api.Order;
import com.oanda.fxtrade.api.RateTable;
import com.oanda.fxtrade.api.RateTableException;

import wenaaa.oandatrading.properties.PropertyManager;

public class TestOrdersPoster {

	@Test
	public void testIsConflictingTrade() {
		final OrdersPoster orderposter = mock(OrdersPoster.class);
		when(orderposter.isConflictingTrade(any(Order.class))).thenCallRealMethod();
		when(orderposter.getAsk()).thenReturn(1.1105);
		when(orderposter.getBid()).thenReturn(1.1100);
		when(orderposter.getDistanceKoef()).thenReturn(4.0);

		// BUY 0.0005 * 4 > 0.002 > needed two space > conflicting trade from 1.1105 to
		// 1.1145
		when(orderposter.isBuyPair()).thenReturn(true);
		final Order b = mock(Order.class);
		when(b.getPrice()).thenReturn(1.11049).thenReturn(1.11051).thenReturn(1.11449).thenReturn(1.11451);
		assertFalse(orderposter.isConflictingTrade(b));
		assertTrue(orderposter.isConflictingTrade(b));
		assertTrue(orderposter.isConflictingTrade(b));
		assertFalse(orderposter.isConflictingTrade(b));

		// SELL 0.0005 * 4 > 0.002 > needed two space > conflicting trade from 1.1060 to
		// 1.1100
		when(orderposter.isBuyPair()).thenReturn(false);
		final Order s = mock(Order.class);
		when(s.getPrice()).thenReturn(1.10599).thenReturn(1.10601).thenReturn(1.10999).thenReturn(1.11001);
		assertFalse(orderposter.isConflictingTrade(s));
		assertTrue(orderposter.isConflictingTrade(s));
		assertTrue(orderposter.isConflictingTrade(s));
		assertFalse(orderposter.isConflictingTrade(s));
	}

	@Test
	public void testGetTradesAndOrders() throws AccountException {
		final OrdersPoster orderposter = mock(OrdersPoster.class);
		when(orderposter.getTradesAndOrders()).thenCallRealMethod();
		final Account acc = mock(Account.class);
		when(orderposter.getAcc()).thenReturn(acc);
		final Vector<Order> trades = getTrades();
		when(acc.getTrades()).thenReturn(trades);
		final Vector<Order> orders = getOrders();
		when(acc.getOrders()).thenReturn(orders);
		final List<Order> to = orderposter.getTradesAndOrders();
		assertEquals(3, to.size());
		int nLO = 0;
		int nMO = 0;
		for (final Order o : to) {
			if (o instanceof LimitOrder) {
				nLO++;
			}
			if (o instanceof MarketOrder) {
				nMO++;
			}
		}
		assertEquals(2, nLO);
		assertEquals(1, nMO);
		when(acc.getTrades()).thenThrow(AccountException.class);
		assertEquals(0, orderposter.getTradesAndOrders().size());
	}

	private Vector<Order> getOrders() {
		final Vector<Order> answ = new Vector<>();
		answ.add(mock(LimitOrder.class));
		answ.add(mock(LimitOrder.class));
		return answ;
	}

	private Vector<Order> getTrades() {
		final Vector<Order> answ = new Vector<>();
		answ.add(mock(MarketOrder.class));
		return answ;
	}

	@Test
	public void testGetTradedPrice() {
		final OrdersPoster orderposter = mock(OrdersPoster.class);
		when(orderposter.getTradePrice()).thenCallRealMethod();
		when(orderposter.getAsk()).thenReturn(1.5);
		when(orderposter.getBid()).thenReturn(1.4);
		when(orderposter.getDistanceKoef()).thenReturn(5.0);

		// (1.5 - 1.4)*5 + 1.5 = 2
		when(orderposter.isBuyPair()).thenReturn(true);
		assertEquals(2.0, orderposter.getTradePrice(), 1e-9);

		// 1.4 - (1.5 - 1.4)*5 = 0.9
		when(orderposter.isBuyPair()).thenReturn(false);
		assertEquals(0.9, orderposter.getTradePrice(), 1e-9);
	}

	@Test
	public void testTrade() {
		final OrdersPoster orderposter = mock(OrdersPoster.class);
		doCallRealMethod().when(orderposter).trade();
		final List<Order> to = getTO();
		when(orderposter.getTradesAndOrders()).thenReturn(to);
		when(orderposter.isConflictingTrade(any(Order.class))).thenReturn(false).thenReturn(true);
		orderposter.trade();
		verify(orderposter, times(2)).isConflictingTrade(any(Order.class));
		verify(orderposter, never()).postNewTrade();
	}

	private List<Order> getTO() {
		final List<Order> list = new ArrayList<>();
		list.add(mock(LimitOrder.class));
		list.add(mock(MarketOrder.class));
		list.add(mock(MarketOrder.class));
		return list;
	}

	@Test
	public void testPostNewTrade() throws OAException {
		final OrdersPoster orderposter = mock(OrdersPoster.class);
		doCallRealMethod().when(orderposter).postNewTrade();
		final LimitOrder lo = mock(LimitOrder.class);
		when(orderposter.createLimitOrder()).thenReturn(lo);
		final FXPair fxpair = mock(FXPair.class);
		when(orderposter.getPair()).thenReturn(fxpair);
		when(orderposter.getUnits()).thenReturn(-2L);
		when(orderposter.getTradePrice()).thenReturn(1.123);
		final Account acc = mock(Account.class);
		when(orderposter.getAcc()).thenReturn(acc);
		final long unixExpiry = ZonedDateTime.now().plusMonths(1).toEpochSecond();
		orderposter.postNewTrade();
		verify(lo).setPair(fxpair);
		verify(lo).setUnits(-2L);
		verify(lo).setPrice(1.123);
		verify(lo).setExpiry(unixExpiry);
		verify(acc).execute(lo);
	}

	@Test
	public void testGetUnits() throws RateTableException, AccountException {
		final OrdersPoster orderposter = mock(OrdersPoster.class);
		when(orderposter.getUnits()).thenCallRealMethod();
		when(orderposter.isBuyPair()).thenReturn(true).thenReturn(false);
		when(orderposter.getCoef()).thenReturn(1.2).thenReturn(0.7);
		PropertyManager.setRiskCoef(1.3);
		final Account acc = mock(Account.class);
		when(orderposter.getAcc()).thenReturn(acc);
		when(acc.getBalance()).thenReturn(1234.5).thenReturn(876.5);
		// 1 * 1.2 * 1.3 * 1234.5 = 1925.82
		assertEquals(1925, orderposter.getUnits());
		// -1 * 0.7 * 1.3 * 876.5 = -797.615
		assertEquals(-797, orderposter.getUnits());
	}

	@Test
	public void testGetCoef() throws RateTableException {
		final OrdersPoster orderposter = mock(OrdersPoster.class);
		when(orderposter.getCoef()).thenCallRealMethod();
		final FXPair fxpair = mock(FXPair.class);
		when(orderposter.getUSDPair()).thenReturn(fxpair);
		when(orderposter.isUSDBased(fxpair)).thenReturn(true).thenReturn(false);
		final RateTable rt = mock(RateTable.class);
		when(orderposter.getRateTable()).thenReturn(rt);
		final FXTick tick = mock(FXTick.class);
		when(rt.getRate(fxpair)).thenReturn(tick);
		when(tick.getAsk()).thenReturn(0.753);
		assertEquals(1, orderposter.getCoef(), 1e-6);
		verify(orderposter, never()).getRateTable();
		assertEquals(1 / 0.753, orderposter.getCoef(), 1e-6);
		verify(orderposter, times(1)).getRateTable();
	}

	@Test
	public void testGetUSDPair() {
		final OrdersPoster orderposter = mock(OrdersPoster.class);
		when(orderposter.getUSDPair()).thenCallRealMethod();
		final FXPair pair = mock(FXPair.class);
		when(orderposter.getPair()).thenReturn(pair);
		when(orderposter.isUSDBased(pair)).thenReturn(true).thenReturn(false);
		orderposter.getUSDPair();
		verify(pair, never()).setQuote(any(String.class));
		orderposter.getUSDPair();
		verify(pair, times(1)).setQuote("USD");
	}

	@Test
	public void testIsUSDBased() {
		final OrdersPoster orderposter = mock(OrdersPoster.class);
		final FXPair pair = mock(FXPair.class);
		when(orderposter.isUSDBased(pair)).thenCallRealMethod();
		when(pair.getQuote()).thenReturn("CAD").thenReturn("USD");
		when(pair.getBase()).thenReturn("USD").thenReturn("CAD");
		assertTrue(orderposter.isUSDBased(pair));
		assertFalse(orderposter.isUSDBased(pair));
	}
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.junit.Test;

import com.oanda.fxtrade.api.Account;
import com.oanda.fxtrade.api.AccountException;
import com.oanda.fxtrade.api.FXPair;
import com.oanda.fxtrade.api.LimitOrder;
import com.oanda.fxtrade.api.MarketOrder;
import com.oanda.fxtrade.api.OAException;
import com.oanda.fxtrade.api.Order;

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
		final long unixExpiry = System.currentTimeMillis() / 1000L + 7770000;
		orderposter.postNewTrade();
		verify(lo).setPair(fxpair);
		verify(lo).setUnits(-2L);
		verify(lo).setPrice(1.123);
		verify(lo).setExpiry(unixExpiry);
		verify(acc).execute(lo);
	}
}
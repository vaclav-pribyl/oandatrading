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

import org.junit.Test;

import wenaaa.oandatrading.api.Account;
import wenaaa.oandatrading.api.CandlePoint;
import wenaaa.oandatrading.api.MarketOrder;

public class TestStopLossHandler {

	private static final double slspace = 0.27;

	@Test
	public void testHandleSL() {
		final StopLossHandler slhandler = mock(StopLossHandler.class);
		when(slhandler.getPrice()).thenReturn(0.0).thenReturn(1.0);
		doCallRealMethod().when(slhandler).handleSL();
		slhandler.handleSL();
		verify(slhandler, never()).applySL(any(Double.class));
		slhandler.handleSL();
		verify(slhandler, times(1)).applySL(any(Double.class));
	}

	@Test
	public void testGetPriceBuy() {
		final StopLossHandler slHandler = mock(StopLossHandler.class);
		final List<CandlePoint> bl = getCLB();
		when(slHandler.getcandles()).thenReturn(bl);
		when(slHandler.getPrice()).thenCallRealMethod();
		when(slHandler.isBuyPair()).thenReturn(true);
		when(slHandler.getSLspace()).thenReturn(slspace);
		final double price = slHandler.getPrice();
		assertEquals(7.11 - slspace, price, 1e-9);
	}

	@Test
	public void testGetPriceSell() {
		final StopLossHandler slHandler = mock(StopLossHandler.class);
		final List<CandlePoint> sl = getCLS();
		when(slHandler.getcandles()).thenReturn(sl);
		when(slHandler.getPrice()).thenCallRealMethod();
		when(slHandler.isBuyPair()).thenReturn(false);
		when(slHandler.getSLspace()).thenReturn(slspace);
		final double price = slHandler.getPrice();
		assertEquals(11 + slspace, price, 1e-9);
	}

	private List<CandlePoint> getCLB() {
		final List<CandlePoint> answ = new ArrayList<>();
		answ.add(getCPMock(7, 7.5, 7.8));
		answ.add(getCPMock(7.5, 8, 8.2));
		answ.add(getCPMock(7.11, 9, 11));
		answ.add(getCPMock(8.1, 8.2, 8.3));
		answ.add(getCPMock(8.05, 8.1, 8.15));
		answ.add(getCPMock(8, 9, 10));
		answ.add(getCPMock(8.5, 9.5, 10.5));
		return answ;
	}

	private CandlePoint getCPMock(final double min, final double close, final double max) {
		final CandlePoint cp = mock(CandlePoint.class);
		when(cp.getMin()).thenReturn(min);
		when(cp.getClose()).thenReturn(close);
		when(cp.getMax()).thenReturn(max);
		return cp;
	}

	private List<CandlePoint> getCLS() {
		final List<CandlePoint> answ = new ArrayList<>();
		answ.add(getCPMock(8, 9, 11));
		answ.add(getCPMock(7.5, 8, 9));
		answ.add(getCPMock(6.8, 9, 10.3));
		answ.add(getCPMock(6.5, 7, 10.2));
		answ.add(getCPMock(4, 6, 8));
		answ.add(getCPMock(5, 6, 9));
		answ.add(getCPMock(6, 7, 10));
		answ.add(getCPMock(5, 6.5, 12));
		return answ;
	}

	@Test
	public void testApplySL() {
		final StopLossHandler slHandler = mock(StopLossHandler.class);
		when(slHandler.getOpenTrades()).thenReturn(gOT());
		final Account acc = mock(Account.class);
		when(slHandler.getAcc()).thenReturn(acc);
		doCallRealMethod().when(slHandler).applySL(any(Double.class));
		when(slHandler.isAcceptable(any(Double.class), any(MarketOrder.class))).thenReturn(true).thenReturn(false)
				.thenReturn(true).thenReturn(false).thenReturn(true);
		slHandler.applySL(0);
		verify(acc, times(3)).modifySL(any(MarketOrder.class));
	}

	private List<MarketOrder> gOT() {
		final List<MarketOrder> answ = new ArrayList<>();
		answ.add(mock(MarketOrder.class));
		answ.add(mock(MarketOrder.class));
		answ.add(mock(MarketOrder.class));
		answ.add(mock(MarketOrder.class));
		answ.add(mock(MarketOrder.class));
		return answ;
	}

	@Test
	public void testIsAcceptableBuy() {
		final StopLossHandler slHandler = mock(StopLossHandler.class);
		when(slHandler.isAcceptable(any(Double.class), any(MarketOrder.class))).thenCallRealMethod();
		when(slHandler.getMinProfit()).thenReturn(1.2);
		when(slHandler.isBuyPair()).thenReturn(true);
		final MarketOrder trade = mock(MarketOrder.class);
		when(trade.getPrice()).thenReturn(1.0).thenReturn(3.0);
		when(trade.getStopLoss()).thenReturn(3.0).thenReturn(1.0);
		assertFalse(slHandler.isAcceptable(2, trade));
		assertFalse(slHandler.isAcceptable(2, trade));
		assertFalse(slHandler.isAcceptable(0.5, trade));
		assertFalse(slHandler.isAcceptable(3.5, trade));
		assertTrue(slHandler.isAcceptable(4.3, trade));
	}

	@Test
	public void testIsAcceptableSell() {
		final StopLossHandler slHandler = mock(StopLossHandler.class);
		when(slHandler.isAcceptable(any(Double.class), any(MarketOrder.class))).thenCallRealMethod();
		when(slHandler.isBuyPair()).thenReturn(false);
		when(slHandler.getMinProfit()).thenReturn(1.2);
		final MarketOrder trade = mock(MarketOrder.class);
		when(trade.getPrice()).thenReturn(4.0).thenReturn(2.0);
		when(trade.getStopLoss()).thenReturn(2.0).thenReturn(4.0);
		assertFalse(slHandler.isAcceptable(3, trade));
		assertFalse(slHandler.isAcceptable(3, trade));
		assertFalse(slHandler.isAcceptable(5, trade));
		assertFalse(slHandler.isAcceptable(1, trade));
		assertTrue(slHandler.isAcceptable(0.75, trade));
		when(trade.getStopLoss()).thenReturn(0.0);
		when(trade.getPrice()).thenReturn(4.0);
		assertFalse(slHandler.isAcceptable(3, trade));
		assertTrue(slHandler.isAcceptable(2.75, trade));
	}

}

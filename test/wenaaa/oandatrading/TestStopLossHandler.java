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
import com.oanda.fxtrade.api.CandlePoint;
import com.oanda.fxtrade.api.FXPair;
import com.oanda.fxtrade.api.MarketOrder;
import com.oanda.fxtrade.api.OAException;
import com.oanda.fxtrade.api.StopLossOrder;

public class TestStopLossHandler {

	
	private static final double slspace = 0.27;

	@Test
	public void testHandleSL() throws OAException {
		final StopLossHandler slhandler = mock(StopLossHandler.class);
		final OAException t = new OAException();
		when(slhandler.getPrice()).thenReturn(0.0).thenThrow(t).thenReturn(1.0);
		doCallRealMethod().when(slhandler).handleSL();
		slhandler.handleSL();
		verify(slhandler, never()).applySL(any(Double.class));
		verify(slhandler, never()).log(any(OAException.class));
		slhandler.handleSL();
		verify(slhandler, times(1)).log(t);
		verify(slhandler,never()).applySL(any(Double.class));
		slhandler.handleSL();
		verify(slhandler, times(1)).log(t);
	}

	@Test
	public void testGetPriceBuy() throws OAException {
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
	public void testGetPriceSell() throws OAException {
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
	public void testApplySL() throws OAException{
		final StopLossHandler slHandler = mock(StopLossHandler.class);
		when(slHandler.getOpenTrades()).thenReturn(gOT());
		Account acc = mock(Account.class);
		when(slHandler.getAcc()).thenReturn(acc);
		doCallRealMethod().when(slHandler).applySL(any(Double.class));
		when(slHandler.isAcceptable(any(Double.class), any(MarketOrder.class))).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false).thenReturn(true);
		slHandler.applySL(0);
		verify(acc,times(3)).modify(any(MarketOrder.class));
		verify(slHandler,times(1)).getSLOrder(0);
	}

	private List<MarketOrder> gOT() {
		List<MarketOrder> answ = new ArrayList<>();
		answ.add(mock(MarketOrder.class));
		answ.add(mock(MarketOrder.class));
		answ.add(mock(MarketOrder.class));
		answ.add(mock(MarketOrder.class));
		answ.add(mock(MarketOrder.class));
		return answ;
	}
	
	@Test
	public void testIsAcceptableBuy(){
		final StopLossHandler slHandler = mock(StopLossHandler.class);
		when(slHandler.isAcceptable(any(Double.class), any(MarketOrder.class))).thenCallRealMethod();
		when(slHandler.isBuyPair()).thenReturn(true);
		final StopLossOrder slorder = mock(StopLossOrder.class);
		when(slorder.getPrice()).thenReturn(3.0).thenReturn(1.0);
		MarketOrder trade = mock(MarketOrder.class);
		when(trade.getPrice()).thenReturn(1.0).thenReturn(3.0);
		when(trade.getStopLoss()).thenReturn(slorder);
		assertFalse(slHandler.isAcceptable(2, trade));
		assertFalse(slHandler.isAcceptable(2, trade));
		assertFalse(slHandler.isAcceptable(0.5, trade));
		assertTrue(slHandler.isAcceptable(3.5, trade));
	}
	
	@Test
	public void testIsAcceptableSell(){
		final StopLossHandler slHandler = mock(StopLossHandler.class);
		when(slHandler.isAcceptable(any(Double.class), any(MarketOrder.class))).thenCallRealMethod();
		when(slHandler.isBuyPair()).thenReturn(false);
		final StopLossOrder slorder = mock(StopLossOrder.class);
		when(slorder.getPrice()).thenReturn(4.0).thenReturn(2.0);
		MarketOrder trade = mock(MarketOrder.class);
		when(trade.getPrice()).thenReturn(2.0).thenReturn(4.0);
		when(trade.getStopLoss()).thenReturn(slorder);
		assertFalse(slHandler.isAcceptable(3, trade));
		assertFalse(slHandler.isAcceptable(3, trade));
		assertFalse(slHandler.isAcceptable(5, trade));
		assertTrue(slHandler.isAcceptable(1, trade));
		when(slorder.getPrice()).thenReturn(0.0);
		assertTrue(slHandler.isAcceptable(3, trade));
	}
	
	@Test
	public void testGetOpenTrades() throws AccountException{
		final StopLossHandler slHandler = mock(StopLossHandler.class);
		when(slHandler.getOpenTrades()).thenCallRealMethod();
		Account acc = getAcc();
		when(slHandler.getAcc()).thenReturn(acc);
		when(slHandler.getPair()).thenReturn("AUD/USD");
		List<MarketOrder> aul = slHandler.getOpenTrades();
		checkOpenTrades(3,"AUD/USD",aul);
		when(slHandler.getPair()).thenReturn("EUR/USD");
		List<MarketOrder> eul = slHandler.getOpenTrades();
		checkOpenTrades(2,"EUR/USD",eul);
		when(slHandler.getPair()).thenReturn("GBP/USD");
		List<MarketOrder> gul = slHandler.getOpenTrades();
		checkOpenTrades(1,"GBP/USD",gul);
		when(slHandler.getPair()).thenReturn("USD/CHF");
		List<MarketOrder> ucl = slHandler.getOpenTrades();
		checkOpenTrades(0,"USD/CHF",ucl);
	}

	void checkOpenTrades(int size, String pair, List<MarketOrder> l) {
		assertEquals(size, l.size());
		for(MarketOrder t:l){
			assertEquals(pair, t.getPair().getPair());
		}
	}

	private Account getAcc() throws AccountException {
		Account acc = mock(Account.class);
		Vector trades = getTrades();
		when(acc.getTrades()).thenReturn(trades);
		return acc ;
	}

	private Vector getTrades() {
		Vector vec = new Vector<>();
		vec.add(getMO("AUD/USD"));
		vec.add(getMO("EUR/USD"));
		vec.add(getMO("AUD/USD"));
		vec.add(getMO("AUD/USD"));
		vec.add(getMO("EUR/USD"));
		vec.add(getMO("GBP/USD"));
		return vec;
	}

	private Object getMO(String pair) {
		MarketOrder mo = mock(MarketOrder.class);
		FXPair fxpair = mock(FXPair.class);
		when(mo.getPair()).thenReturn(fxpair);
		when(fxpair.getPair()).thenReturn(pair);
		return mo;
	}
}

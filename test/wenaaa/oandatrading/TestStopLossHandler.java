package wenaaa.oandatrading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

import com.oanda.fxtrade.api.CandlePoint;
import com.oanda.fxtrade.api.OAException;
import com.oanda.fxtrade.api.StopLossOrder;

public class TestStopLossHandler {

	private static final double slspace = 0.27;

	@Test
	public void testGetSlOrderReturn() throws OAException {
		final StopLossHandler slhandler = mock(StopLossHandler.class);
		final StopLossOrder order = mock(StopLossOrder.class);
		when(order.getPrice()).thenReturn(0.0).thenReturn(1.0);
		final OAException t = new OAException();
		when(slhandler.getSLOrderInternal()).thenReturn(order).thenThrow(t).thenReturn(order);
		doCallRealMethod().when(slhandler).getSLOrder();
		assertNull(slhandler.getSLOrder());
		verify(slhandler, never()).isAcceptable(any(StopLossOrder.class));
		verify(slhandler, never()).log(any(OAException.class));
		verify(order, times(1)).getPrice();
		assertNull(slhandler.getSLOrder());
		verify(slhandler, times(1)).log(t);
		verify(order, times(1)).getPrice();
		when(slhandler.isAcceptable(order)).thenReturn(false).thenReturn(true);
		assertNull(slhandler.getSLOrder());
		verify(slhandler, times(1)).log(t);
		verify(order, times(2)).getPrice();
		assertEquals(order, slhandler.getSLOrder());
		verify(slhandler, times(1)).log(t);
		verify(order, times(3)).getPrice();
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

}

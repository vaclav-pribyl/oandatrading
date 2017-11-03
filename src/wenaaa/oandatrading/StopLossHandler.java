package wenaaa.oandatrading;

import java.util.ArrayList;
import java.util.List;

import com.oanda.fxtrade.api.API;
import com.oanda.fxtrade.api.CandlePoint;
import com.oanda.fxtrade.api.FXPair;
import com.oanda.fxtrade.api.Instrument;
import com.oanda.fxtrade.api.OAException;
import com.oanda.fxtrade.api.RateTable;
import com.oanda.fxtrade.api.StopLossOrder;

import wenaaa.loginutils.LoggingUtils;
import wenaaa.oandatrading.properties.PropertyManager;
import wenaaa.oandatrading.properties.SLHandlingProperties;
import wenaaa.oandatrading.properties.TradedPair;

public class StopLossHandler {

	private final String pair;
	private final boolean buyPair;
	private final SLHandlingProperties slHandling;
	private final RateTable rateTable;

	public StopLossHandler(final TradedPair pair, final RateTable rateTable) {
		this.pair = pair.getName();
		this.buyPair = pair.isBuyPair();
		slHandling = PropertyManager.getSLHandlingProperties();
		this.rateTable = rateTable;
	}

	public StopLossOrder getSLOrder() {
		StopLossOrder sl;
		try {
			sl = getSLOrderInternal();
		} catch (final OAException e) {
			log(e);
			return null;
		}
		if (sl.getPrice() == 0) {
			return null;
		}
		if (isAcceptable(sl)) {
			return sl;
		}
		return null;
	}

	void log(final OAException e) {
		LoggingUtils.logInfo("Can not set SL: " + e.getMessage());
		LoggingUtils.logException(e);
	}

	boolean isAcceptable(final StopLossOrder slorder) {
		return false;
	}

	StopLossOrder getSLOrderInternal() throws OAException {
		final double price = getPrice();
		return API.createStopLossOrder(price);
	}

	double getPrice() throws OAException {
		double answ = 0;
		final List<CandlePoint> cpl = getcandles();
		final CandlePoint lcc = cpl.get(cpl.size() - 2); // last closed candle
		final double refClose = lcc.getClose();
		double extrem = isBuyPair() ? lcc.getMin() : lcc.getMax();
		final double refextrem = extrem;
		for (int i = cpl.size() - 3; i >= 0; i--) {
			final CandlePoint cp = cpl.get(i);
			if (isBuyPair() && cp.getMin() < extrem) {
				extrem = cp.getMin();
			}
			if (!isBuyPair() && cp.getMax() > extrem) {
				extrem = cp.getMax();
			}
			if (isBuyPair() && cp.getMax() < refClose && cp.getMin() < refextrem) {
				answ = extrem - getSLspace();
				break;
			}
			if (!isBuyPair() && cp.getMin() > refClose && cp.getMax() > refextrem) {
				answ = extrem + getSLspace();
				break;
			}
		}
		return answ;
	}

	boolean isBuyPair() {
		return buyPair;
	}

	double getSLspace() {
		final Instrument instrument = rateTable.getInstrument(pair);
		final double ask = instrument.getAsk();
		final double bid = instrument.getBid();
		return (ask - bid) * slHandling.getAddedSpaceCoef();
	}

	List<CandlePoint> getcandles() throws OAException {
		return new ArrayList<>(
				rateTable.getCandles(getAPIPair(), slHandling.getTimeFrameValue(), slHandling.getCandles()));
	}

	private FXPair getAPIPair() {
		return API.createFXPair(pair);
	}
}

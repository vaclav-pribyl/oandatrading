package wenaaa.oandatrading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wenaaa.loginutils.LoggingUtils;
import wenaaa.oandatrading.api.API;
import wenaaa.oandatrading.api.Account;
import wenaaa.oandatrading.api.CandlePoint;
import wenaaa.oandatrading.api.FXPair;
import wenaaa.oandatrading.api.MarketOrder;
import wenaaa.oandatrading.api.RateTable;
import wenaaa.oandatrading.api.TradeApiException;
import wenaaa.oandatrading.properties.PropertyManager;
import wenaaa.oandatrading.properties.SLHandlingProperties;
import wenaaa.oandatrading.properties.TradedPair;

public class StopLossHandler {

	private final String pair;
	private final boolean buyPair;
	private final SLHandlingProperties slHandling;
	private final RateTable rateTable;
	private final Account account;
	private final FXPair fxPair;

	public StopLossHandler(final TradedPair pair, final Account acc) {
		this.pair = pair.getName();
		this.buyPair = pair.isBuyPair();
		slHandling = PropertyManager.getSLHandlingProperties();
		this.rateTable = API.createRateTable(acc.getID());
		account = acc;
		fxPair = API.createFXPair(acc.getID(), pair.getName());
	}

	public void handleSL() {
		double price;
		price = getPrice();
		if (price == 0) {
			return;
		}
		applySL(price);
		return;
	}

	void applySL(final double price) {
		final List<? extends MarketOrder> ot = getOpenTrades();
		for (final MarketOrder trade : ot) {
			if (isAcceptable(price, trade)) {
				try {
					trade.setStopLossPrice(price);
					LoggingUtils.logInfo("SSL > " + trade);
					getAcc().modifySL(trade);
					LoggingUtils.logInfo("OK");
				} catch (final TradeApiException e) {
					LoggingUtils.logInfo("Can't set SL > " + e.getMessage());
				}
			}
		}

	}

	Account getAcc() {
		return account;
	}

	List<MarketOrder> getOpenTrades() {
		try {
			return getAcc().getTrades(pair);
		} catch (final TradeApiException e) {
			LoggingUtils.logException(e);
			LoggingUtils.logInfo("Can't get trade list: " + e.getMessage());
			return Collections.emptyList();
		}
	}

	String getPair() {
		return pair;
	}

	boolean isAcceptable(final double price, final MarketOrder trade) {
		final double stopLoss = trade.getStopLoss();
		if (isBuyPair()) {
			return (price - trade.getPrice()) > getMinProfit() && (price - stopLoss) >= getMinSLChange();
		}
		if (stopLoss != 0) {
			return (trade.getPrice() - price) > getMinProfit() && (stopLoss - price) >= getMinSLChange();
		}
		return (trade.getPrice() - price) > getMinProfit();
	}

	double getMinSLChange() {
		return Account.getPipette(pair);
	}

	double getMinProfit() {
		return PropertyManager.getMinProfitCoef() * PropertyManager.getDistanceKoef() * getSpread();
	}

	double getPrice() {
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
		return getSpread() * slHandling.getAddedSpaceCoef();
	}

	double getSpread() {
		final double ask = fxPair.getAsk();
		final double bid = fxPair.getBid();
		return ask - bid;
	}

	List<CandlePoint> getcandles() {
		return new ArrayList<>(rateTable.getCandles(getAPIPair(), slHandling.getTimeFrame(), slHandling.getCandles()));
	}

	FXPair getAPIPair() {
		return fxPair;
	}

}

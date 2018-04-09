package wenaaa.oandatrading;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import wenaaa.loginutils.LoggingUtils;
import wenaaa.oandatrading.api.API;
import wenaaa.oandatrading.api.Account;
import wenaaa.oandatrading.api.FXPair;
import wenaaa.oandatrading.api.LimitOrder;
import wenaaa.oandatrading.api.Order;
import wenaaa.oandatrading.api.RateTable;
import wenaaa.oandatrading.api.TradeApiException;
import wenaaa.oandatrading.properties.PropertyManager;
import wenaaa.oandatrading.properties.TradedPair;

public class OrdersPoster {

	private final String pair;
	private final boolean buyPair;
	private final RateTable rateTable;
	private final Account account;
	private final double distance_koef;

	public OrdersPoster(final TradedPair pair, final RateTable rateTable, final Account acc) {
		this.pair = pair.getName();
		this.buyPair = pair.isBuyPair();
		this.rateTable = rateTable;
		account = acc;
		distance_koef = PropertyManager.getDistanceKoef();
	}

	public void trade() {
		for (final Order t : getTradesAndOrders()) {
			if (isConflictingTrade(t)) {
				return;
			}
		}
		postNewTrade();
	}

	void postNewTrade() {
		try {
			final LimitOrder limitOrder = createLimitOrder();

			limitOrder.setPair(getPair());
			limitOrder.setUnits(getUnits());
			limitOrder.setPrice(getTradePrice());
			limitOrder.setExpiry(ZonedDateTime.now().plusMonths(1).toEpochSecond());

			LoggingUtils.logInfo("Submitting limit order..." + limitOrder);
			getAcc().execute(limitOrder);
			LoggingUtils.logInfo("Limit order entered successfully");
		} catch (final Exception e) {
			LoggingUtils.logInfo("Error: limit order execution failed: " + e);
		}
	}

	LimitOrder createLimitOrder() {
		return API.createLimitOrder();
	}

	FXPair getPair() {
		return API.createFXPair(pair);
	}

	double getDistanceKoef() {
		return distance_koef;
	}

	double getAsk() {
		return getRateTable().getInstrument(pair).getAsk();
	}

	double getBid() {
		return getRateTable().getInstrument(pair).getBid();
	}

	boolean isConflictingTrade(final Order trade) {
		final double ask = getAsk();
		final double bid = getBid();
		final double distance = getDistanceKoef() * (ask - bid);
		final double tradePrice = trade.getPrice();
		if (isBuyPair()) {
			return tradePrice > ask && tradePrice < ask + 2 * distance;
		}
		return tradePrice < bid && tradePrice > bid - 2 * distance;
	}

	List<Order> getTradesAndOrders() {
		final List<Order> answ = new ArrayList<>();
		try {
			answ.addAll(getAcc().getTrades());
			answ.addAll(getAcc().getOrders());
		} catch (final TradeApiException e) {
			LoggingUtils.logException(e);
			LoggingUtils.logInfo("Can't get orders and/or trades: " + e.getMessage());
		}
		return answ;
	}

	long getUnits() {
		final int buycoef = isBuyPair() ? 1 : -1;
		final double ratecoef = getCoef();
		final double riskcoef = PropertyManager.getRiskCoef();
		final Account acc = getAcc();
		final double nav = acc.getBalance() + acc.getUnrealizedPL();
		final double answ = nav * buycoef * ratecoef * riskcoef;
		return (long) answ;
	}

	double getCoef() {
		final FXPair fxpair = getUSDPair();
		if (isUSDBased(fxpair)) {
			return 1;
		}
		final double rate = getRateTable().getRate(fxpair).getAsk();
		return 1 / rate;
	}

	RateTable getRateTable() {
		return rateTable;
	}

	FXPair getUSDPair() {
		final FXPair fxpair = getPair();
		if (isUSDBased(fxpair)) {
			return fxpair;
		}
		fxpair.setQuote("USD");
		return fxpair;
	}

	boolean isUSDBased(final FXPair fxpair) {
		return "USD".equals(fxpair.getBase());
	}

	double getTradePrice() {
		final double ask = getAsk();
		final double bid = getBid();
		final double distance = getDistanceKoef() * (ask - bid);
		return isBuyPair() ? ask + distance : bid - distance;
	}

	Account getAcc() {
		return account;
	}

	boolean isBuyPair() {
		return buyPair;
	}
}

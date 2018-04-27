package wenaaa.oandatrading;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import wenaaa.loginutils.LoggingUtils;
import wenaaa.oandatrading.api.API;
import wenaaa.oandatrading.api.Account;
import wenaaa.oandatrading.api.FXPair;
import wenaaa.oandatrading.api.Order;
import wenaaa.oandatrading.api.StopOrder;
import wenaaa.oandatrading.api.TradeApiException;
import wenaaa.oandatrading.properties.PropertyManager;
import wenaaa.oandatrading.properties.TradedPair;

public class OrdersPoster {

	private final boolean buyPair;
	private final Account account;
	private final double distance_koef;
	private final FXPair fxPair;

	public OrdersPoster(final TradedPair pair, final Account acc) {
		this.buyPair = pair.isBuyPair();
		account = acc;
		distance_koef = PropertyManager.getDistanceKoef();
		fxPair = API.createFXPair(account.getID(), pair.getName());
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
			final StopOrder limitOrder = createStopOrder();

			limitOrder.setPair(getPair());
			limitOrder.setUnits(getUnits());
			limitOrder.setPrice(getTradePrice());
			limitOrder.setExpiry(ZonedDateTime.now().plusMonths(1));

			LoggingUtils.logInfo("Submitting new order..." + limitOrder);
			getAcc().execute(limitOrder);
			LoggingUtils.logInfo("Order entered successfully");
		} catch (final Exception e) {
			LoggingUtils.logInfo("Error: Order execution failed: " + e);
		}
	}

	StopOrder createStopOrder() {
		return API.createLimitOrder();
	}

	FXPair getPair() {
		return fxPair;
	}

	double getDistanceKoef() {
		return distance_koef;
	}

	double getAsk() {
		return fxPair.getAsk();
	}

	double getBid() {
		return fxPair.getBid();
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
		final double ratecoef = getRateCoef();
		final double riskcoef = PropertyManager.getRiskCoef();
		final Account acc = getAcc();
		final double nav = acc.getBalance() + acc.getUnrealizedPL();
		final double answ = nav * buycoef * ratecoef * riskcoef;
		return (long) answ;
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

	double getRateCoef() {
		return fxPair.getRateCoef();
	}
}

package wenaaa.oandatrading;

import java.util.ArrayList;
import java.util.List;

import com.oanda.fxtrade.api.API;
import com.oanda.fxtrade.api.Account;
import com.oanda.fxtrade.api.AccountException;
import com.oanda.fxtrade.api.FXPair;
import com.oanda.fxtrade.api.LimitOrder;
import com.oanda.fxtrade.api.Order;
import com.oanda.fxtrade.api.RateTable;

import wenaaa.loginutils.LoggingUtils;
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
			limitOrder.setExpiry(System.currentTimeMillis() / 1000L + 7770000);

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
		return rateTable.getInstrument(pair).getAsk();
	}

	double getBid() {
		return rateTable.getInstrument(pair).getBid();
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
		} catch (final AccountException e) {
			LoggingUtils.logException(e);
			LoggingUtils.logInfo("Can't get orders and/or trades: " + e.getMessage());
		}
		return answ;
	}

	long getUnits() {
		return isBuyPair() ? 1 : -1;
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

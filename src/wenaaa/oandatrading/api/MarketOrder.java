package wenaaa.oandatrading.api;

import java.time.Instant;

import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.OrderID;
import com.oanda.v20.order.OrderSpecifier;
import com.oanda.v20.order.StopLossOrder;
import com.oanda.v20.order.TimeInForce;
import com.oanda.v20.trade.TradeID;
import com.oanda.v20.trade.TradeSetDependentOrdersRequest;
import com.oanda.v20.trade.TradeSpecifier;
import com.oanda.v20.trade.TradeSummary;
import com.oanda.v20.transaction.StopLossDetails;

public class MarketOrder implements Order {

	private final FXPair pair;
	private final TradeSummary trade;
	private double sl;
	private final String accountID;

	public MarketOrder(final String accountID, final TradeSummary trade) {
		this.trade = trade;
		pair = new FXPair(accountID, trade.getInstrument().toString());
		this.accountID = accountID;
	}

	private long parseOpenTime() {
		final String timeString = trade.getOpenTime().toString();
		long pomtime;
		try {
			pomtime = Long.parseLong(timeString);
		} catch (final NumberFormatException nfe) {
			pomtime = Instant.parse(timeString).toEpochMilli() / 1000;
		}
		return pomtime;
	}

	public long getTimestamp() {
		return parseOpenTime();
	}

	TradeID getID() {
		return trade.getId();
	}

	public FXPair getPair() {
		return pair;
	}

	public double getStopLoss() {
		final OrderID slid = trade.getStopLossOrderID();
		if (slid == null || "".equals(slid.toString())) {
			return 0;
		}
		final OrderSpecifier orderSpecifier = new OrderSpecifier(slid);
		try {
			final com.oanda.v20.order.Order order = API.getContext().order.get(new AccountID(accountID), orderSpecifier)
					.getOrder();
			if (order instanceof StopLossOrder) {
				final double answ = ((StopLossOrder) order).getPrice().doubleValue();
			}
			return 0;
		} catch (RequestException | ExecuteException e) {
			throw new TradeApiException(e);
		}
	}

	public double getUnrealizedPL() {
		return trade.getUnrealizedPL().doubleValue();
	}

	@Override
	public double getPrice() {
		return trade.getPrice().doubleValue();
	}

	public void setStopLossPrice(final double sl) {
		this.sl = sl;
	}

	TradeSetDependentOrdersRequest createStopLossRequest(final AccountID accountID) {
		final TradeSpecifier tradeSpecifier = new TradeSpecifier(getID());
		final TradeSetDependentOrdersRequest request = new TradeSetDependentOrdersRequest(accountID, tradeSpecifier);
		final StopLossDetails stopLoss = new StopLossDetails();
		stopLoss.setPrice(getPriceString());
		stopLoss.setTimeInForce(TimeInForce.GTC);
		request.setStopLoss(stopLoss);
		return request;
	}

	public boolean hasSL() {
		final OrderID slid = trade.getStopLossOrderID();
		return !(slid == null || "".equals(slid.toString()));
	}

	String getPriceString() {
		final String priceString = String.valueOf(sl);
		final int point = priceString.indexOf('.');
		final int endIndex = Math.min(priceString.length(), point + getDisplayPrecission(pair.getPair()) + 1);
		return priceString.substring(0, endIndex);
	}

	int getDisplayPrecission(final String pair) {
		return Account.getDisplayPrecision(pair);
	}

	@Override
	public String toString() {
		return "StopOrder > " + pair.getPair() + " / " + trade.getId() + " / " + trade.getPrice() + " / "
				+ trade.getInitialUnits() + " / " + sl;
	}

	public String getSummaryInfo() {
		return trade.toString();
	}
}

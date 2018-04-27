package wenaaa.oandatrading.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountGetResponse;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountInstrumentsResponse;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderType;
import com.oanda.v20.order.StopOrderRequest;
import com.oanda.v20.primitives.Instrument;
import com.oanda.v20.trade.TradeSetDependentOrdersRequest;
import com.oanda.v20.trade.TradeSpecifier;
import com.oanda.v20.trade.TradeSummary;

public class Account {

	private AccountSummary summary;

	private long summaryTimeStamp;
	private long detailTimeStamp;
	private final String id;

	private AccountGetResponse detail;

	private static AccountInstrumentsResponse instruments;

	Account(final String id) {
		this.id = id;
		getInstruments(id);
	}

	private static synchronized void getInstruments(final String id) {
		if (instruments == null) {
			try {
				instruments = API.getContext().account.instruments(new AccountID(id));
			} catch (RequestException | ExecuteException e) {
				throw new TradeApiException(e);
			}
		}
	}

	public String getID() {
		return id;
	}

	private synchronized AccountSummary getSummary() {
		if (summary == null || isOld(true)) {
			try {
				summary = API.getContext().account.summary(new AccountID(id)).getAccount();
				summaryTimeStamp = System.currentTimeMillis();
			} catch (RequestException | ExecuteException e) {
				throw new TradeApiException(e);
			}
		}
		return summary;
	}

	private synchronized AccountGetResponse getDetail() {
		if (detail == null || isOld(false)) {
			try {
				detail = API.getContext().account.get(new AccountID(id));
				detailTimeStamp = System.currentTimeMillis();
			} catch (RequestException | ExecuteException e) {
				throw new TradeApiException(e);
			}
		}
		return detail;
	}

	private boolean isOld(final boolean sum) {
		if (sum) {
			return (System.currentTimeMillis() - summaryTimeStamp) > 5 * 60 * 1000;
		} else {
			return (System.currentTimeMillis() - detailTimeStamp) > 1000;
		}
	}

	public Integer getOrdersCount() {
		return getSummary().getPendingOrderCount();
	}

	public double getUnrealizedPL() {
		return getSummary().getUnrealizedPL().doubleValue();
	}

	public double getBalance() {
		return getSummary().getBalance().doubleValue();
	}

	public double getMarginUsed() {
		return getSummary().getMarginUsed().doubleValue();
	}

	public double getMarginAvailable() {
		return getSummary().getMarginAvailable().doubleValue();
	}

	public double getPositionValue() {
		return getSummary().getPositionValue().doubleValue();
	}

	public String getHomeCurrency() {
		return getSummary().getCurrency().toString();

	}

	public void close(final MarketOrder trade) {
		final TradeSpecifier tradeSpecifier = new TradeSpecifier(trade.getID());
		try {
			API.getContext().trade.get(new AccountID(id), tradeSpecifier);
		} catch (RequestException | ExecuteException e) {
			throw new TradeApiException(e);
		}

	}

	public void execute(final StopOrder limitOrder) {
		final OrderCreateRequest request = new OrderCreateRequest(new AccountID(id));
		final StopOrderRequest orderRequest = limitOrder.createStopOrderRequest();
		request.setOrder(orderRequest);
		try {
			API.getContext().order.create(request);
		} catch (RequestException | ExecuteException e) {
			throw new TradeApiException(e);
		}
	}

	public void modifySL(final MarketOrder trade) {
		final TradeSetDependentOrdersRequest request = trade.createStopLossRequest(new AccountID(id));
		try {
			API.getContext().trade.setDependentOrders(request);
		} catch (RequestException | ExecuteException e) {
			throw new TradeApiException(e);
		}
	}

	public Collection<Order> getOrders() {
		final List<com.oanda.v20.order.Order> orders = getDetail().getAccount().getOrders();
		final Collection<Order> answ = new ArrayList<>(orders.size());
		for (final com.oanda.v20.order.Order order : orders) {
			if (order.getType() == OrderType.STOP) {
				final StopOrder so = new StopOrder();
				so.setPrice(((com.oanda.v20.order.StopOrder) order).getPrice().doubleValue());
				answ.add(so);
			}
		}
		return answ;
	}

	public Collection<MarketOrder> getTrades() {
		return getTrades(null);
	}

	public List<MarketOrder> getTrades(final String pair) {
		final List<TradeSummary> trades = getDetail().getAccount().getTrades();
		final List<MarketOrder> answ = new ArrayList<>();
		for (final TradeSummary trade : trades) {
			if (pair == null || trade.getInstrument().equals(pair)) {
				answ.add(new MarketOrder(id, trade));
			}
		}
		return answ;
	}

	public static double getPipette(final String pair) {
		final int coef = getDisplayPrecision(pair);
		return Math.pow(10, -coef);
	}

	static int getDisplayPrecision(final String pair) {
		int coef = 5;
		for (final Instrument instr : instruments.getInstruments()) {
			if (instr.getName().equals(pair)) {
				coef = instr.getDisplayPrecision();
				break;
			}
		}
		return coef;
	}
}

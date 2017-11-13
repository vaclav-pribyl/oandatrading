package wenaaa.oandatrading;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.oanda.fxtrade.api.Account;
import com.oanda.fxtrade.api.AccountException;
import com.oanda.fxtrade.api.MarketOrder;
import com.oanda.fxtrade.api.OAException;
import com.oanda.fxtrade.api.RateTable;
import com.oanda.fxtrade.api.RateTableException;

import wenaaa.loginutils.LoggingUtils;

public class TradesCloser {

	private final Collection<Account> accounts;
	private final RateTable rateTable;

	public TradesCloser(final Collection<Account> accounts, final RateTable rateTable) {
		this.accounts = accounts;
		this.rateTable = rateTable;
	}

	public void closeTrades() throws AccountException {
		for (final Account acc : getAccounts()) {
			closeAccountTrades(acc);
		}
	}

	void closeAccountTrades(final Account acc) throws AccountException {
		final Map<String, MarketOrder> worsts = new HashMap<>();
		for (final MarketOrder trade : getOpenTrades(acc)) {
			if (hasSL(trade)) {
				continue;
			}
			if (trade.getTimestamp() < getTimeStop()) {
				try {
					LoggingUtils.logInfo("Closing too old > " + trade);
					acc.close(trade);
				} catch (final OAException e) {
					LoggingUtils.logInfo("Can't close trade " + e.getMessage());
				}
				continue;
			}
			final String pair = getPair(trade);
			if (!worsts.containsKey(pair) || getUPL(trade) < getUPL(worsts.get(pair))) {
				worsts.put(pair, trade);
			}
		}
		for (final MarketOrder worst : worsts.values()) {
			try {
				LoggingUtils.logInfo("Closing worst > " + worst);
				acc.close(worst);
			} catch (final OAException e) {
				LoggingUtils.logInfo("Can't close trade " + e.getMessage());
			}
		}
	}

	String getPair(final MarketOrder trade) {
		return trade.getPair().getPair();
	}

	boolean hasSL(final MarketOrder trade) {
		return trade.getStopLoss().getPrice() != 0;
	}

	double getUPL(final MarketOrder trade) {
		try {
			return trade.getUnrealizedPL(getRateTable().getRate(trade.getPair()));
		} catch (final RateTableException e) {
			return 0;
		}
	}

	RateTable getRateTable() {
		return rateTable;
	}

	Collection<Account> getAccounts() {
		return accounts;
	}

	Vector<MarketOrder> getOpenTrades(final Account acc) throws AccountException {
		return acc.getTrades();
	}

	long getTimeStop() {
		return ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusDays(10).toEpochSecond();
	}

}

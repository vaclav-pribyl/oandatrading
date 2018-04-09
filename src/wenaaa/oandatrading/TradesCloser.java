package wenaaa.oandatrading;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import wenaaa.loginutils.LoggingUtils;
import wenaaa.oandatrading.api.Account;
import wenaaa.oandatrading.api.MarketOrder;
import wenaaa.oandatrading.api.RateTable;
import wenaaa.oandatrading.api.TradeApiException;

public class TradesCloser {

	private final Collection<Account> accounts;
	private final RateTable rateTable;

	public TradesCloser(final Collection<Account> accounts, final RateTable rateTable) {
		this.accounts = accounts;
		this.rateTable = rateTable;
	}

	public void closeTrades() {
		for (final Account acc : getAccounts()) {
			closeAccountTrades(acc);
		}
	}

	void closeAccountTrades(final Account acc) {
		final Map<String, MarketOrder> worsts = new HashMap<>();
		for (final MarketOrder trade : getOpenTrades(acc)) {
			if (hasSL(trade)) {
				continue;
			}
			if (trade.getTimestamp() < getTimeStop()) {
				try {
					LoggingUtils.logInfo("Closing too old > " + trade);
					acc.close(trade);
				} catch (final TradeApiException e) {
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
			} catch (final TradeApiException e) {
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
		} catch (final TradeApiException e) {
			return 0;
		}
	}

	RateTable getRateTable() {
		return rateTable;
	}

	Collection<Account> getAccounts() {
		return accounts;
	}

	Collection<MarketOrder> getOpenTrades(final Account acc) {
		return acc.getTrades();
	}

	long getTimeStop() {
		return ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusDays(10).toEpochSecond();
	}

}

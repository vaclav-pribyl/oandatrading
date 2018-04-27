package wenaaa.oandatrading;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import wenaaa.loginutils.LoggingUtils;
import wenaaa.oandatrading.api.API;
import wenaaa.oandatrading.api.Account;
import wenaaa.oandatrading.api.MarketOrder;
import wenaaa.oandatrading.api.RateTable;
import wenaaa.oandatrading.api.TradeApiException;

public class TradesCloser {

	private final Collection<Account> accounts;

	public TradesCloser(final Collection<Account> accounts) {
		this.accounts = accounts;
	}

	public void closeTrades() {
		for (final Account acc : getAccounts()) {
			closeAccountTrades(acc);
		}
	}

	void closeAccountTrades(final Account acc) {
		final Map<String, MarketOrder> worsts = new HashMap<>();
		for (final MarketOrder trade : getOpenTrades(acc)) {
			if (trade.hasSL()) {
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
			if (!worsts.containsKey(pair) || trade.getUnrealizedPL() < worsts.get(pair).getUnrealizedPL()) {
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

	RateTable getRateTable(final Account acc) {
		return API.createRateTable(acc.getID());
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

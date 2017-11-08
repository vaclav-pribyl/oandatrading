package wenaaa.oandatrading;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Vector;

import com.oanda.fxtrade.api.Account;
import com.oanda.fxtrade.api.MarketOrder;
import com.oanda.fxtrade.api.OAException;
import com.oanda.fxtrade.api.RateTable;
import com.oanda.fxtrade.api.RateTableException;

import wenaaa.loginutils.LoggingUtils;

public class TradesCloser {

	private Collection<Account> accounts;
	private RateTable rateTable;

	public void closeTrades() {
		for (final Account acc : getAccounts()) {
			closeAccountTrades(acc);
		}
	}

	void closeAccountTrades(final Account acc) {
		MarketOrder worst = null;
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
			if (worst == null || getUPL(trade) < getUPL(worst)) {
				worst = trade;
			}
		}
		if (worst != null) {
			try {
				LoggingUtils.logInfo("Closing worst > " + worst);
				acc.close(worst);
			} catch (final OAException e) {
				LoggingUtils.logInfo("Can't close trade " + e.getMessage());
			}
		}
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

	Vector<MarketOrder> getOpenTrades(final Account acc) {
		return null;
	}

	long getTimeStop() {
		return ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusDays(10).toEpochSecond();
	}

}

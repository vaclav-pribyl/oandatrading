package wenaaa.oandatrading.api;

public class FXPair {

	private final String base;
	private final String quote;
	private final RateTable rateTable;
	private final String homeCurrency;
	private final String accountID;

	FXPair(final String accountID, final String pair) {
		final String[] tokens = pair.split("_");
		quote = tokens[0];
		base = tokens[1];
		rateTable = API.createRateTable(accountID);
		homeCurrency = API.createAccount(accountID).getHomeCurrency();
		this.accountID = accountID;
	}

	FXPair(final String accountID, final String quote, final String base) {
		this.quote = quote;
		this.base = base;
		rateTable = API.createRateTable(accountID);
		homeCurrency = API.createAccount(accountID).getHomeCurrency();
		this.accountID = accountID;
	}

	public String getPair() {
		return quote + "_" + base;
	}

	public double getRateCoef() {
		return getRateCoef(false);
	}

	double getRateCoef(final boolean info) {
		// for interactive junits
		if (quote.equals(homeCurrency)) {
			if (info) {
				System.out.println("\tUSD pair");
			}
			return 1;
		}
		if (base.equals(homeCurrency)) {
			return 2 / (getAsk() + getBid());
		}
		try {
			final FXPair pair = new FXPair(accountID, quote, homeCurrency);
			if (info) {
				System.out.println("\tusing pair " + pair.getPair() + " > " + pair.getAsk());
			}
			return 2 / (pair.getAsk() + pair.getBid());
		} catch (final Exception e) {
			final FXPair pair = new FXPair(accountID, homeCurrency, quote);
			if (info) {
				System.out.println("\tusing pair " + pair.getPair() + " > " + pair.getAsk());
			}
			return (pair.getAsk() + pair.getBid()) / 2;
		}
	}

	public double getAsk() {
		return rateTable.getRate(this).getAsk();
	}

	public double getBid() {
		return rateTable.getRate(this).getBid();
	}
}

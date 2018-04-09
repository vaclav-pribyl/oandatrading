package wenaaa.oandatrading.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PropertyManager {

	private static final File pf = new File("settings.xml");
	private static final Set<AccountProperties> accounts = new HashSet<>();
	private static SLHandlingProperties slProperties;
	private static double distancekoef;
	private static double resetBalanceRatio;
	private static double riskCoef;
	private static double minprofitcoef;
	private static String loginToken;

	private PropertyManager() {

	}

	public static void loadSettings() {
		new PropertiesHandler(pf).load();
	}

	public static void clearSettings() {
		accounts.clear();
		slProperties = null;
		distancekoef = 0;
		resetBalanceRatio = 0;
		riskCoef = 0;
		minprofitcoef = 0;
		loginToken = "";
	}

	public static Collection<String> getAccounts() {
		final Collection<String> answ = new ArrayList<>();
		for (final AccountProperties acc : accounts) {
			answ.add(acc.id);
		}
		return answ;
	}

	public static Collection<TradedPair> getTradedPairs(final String accountId) {
		for (final AccountProperties acc : accounts) {
			if (accountId.equals(acc.id)) {
				return Collections.unmodifiableCollection(acc.pairs);
			}
		}
		return Collections.emptyList();
	}

	public static SLHandlingProperties getSLHandlingProperties() {
		return slProperties;
	}

	public static void setSLHandlingProperties(final SLHandlingProperties properties) {
		slProperties = properties;
	}

	public static void addAccount(final String account_id) {
		accounts.add(new AccountProperties(account_id));
	}

	public static void addTradedPair(final String account_id, final String pair, final String position) {
		for (final AccountProperties acc : accounts) {
			if (acc.id.equals(account_id)) {
				acc.addPair(pair, position);
				break;
			}
		}
	}

	public static double getDistanceKoef() {
		return distancekoef;
	}

	public static void setDistanceKoef(final double dk) {
		distancekoef = dk;
	}

	public static double getResetBalanceRatio() {
		return resetBalanceRatio;
	}

	public static String getLoginToken() {
		return loginToken;
	}

	public static void setLoginToken(final String loginToken) {
		PropertyManager.loginToken = loginToken;
	}

	public static void setResetBalanceRatio(final double rbr) {
		resetBalanceRatio = rbr;
	}

	public static double getRiskCoef() {
		return riskCoef;
	}

	public static void setRiskCoef(final double rc) {
		riskCoef = rc;
	}

	public static double getMinProfitCoef() {
		return minprofitcoef;
	}

	public static void setMinProfitCoef(final double mpc) {
		minprofitcoef = mpc;
	}
}

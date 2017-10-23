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

	private PropertyManager() {

	}

	public static void loadSettings() {
		new PropertiesHandler(pf).load();
	}

	public static void clearSettings() {
		accounts.clear();
	}

	public static Collection<Integer> getAccounts() {
		final Collection<Integer> answ = new ArrayList<>();
		for (final AccountProperties acc : accounts) {
			answ.add(acc.id);
		}
		return answ;
	}

	public static Collection<TradedPair> getTradedPairs(final int account_id) {
		for (final AccountProperties acc : accounts) {
			if (account_id == acc.id) {
				return Collections.unmodifiableCollection(acc.pairs);
			}
		}
		return Collections.emptyList();
	}

	public static SLHandlingProperties getSLHandlingProperties() {
		return slProperties;
	}

	static void setSLHandlingProperties(final SLHandlingProperties properties) {
		slProperties = properties;
	}

	static void addAccount(final int account_id) {
		accounts.add(new AccountProperties(account_id));
	}

	static void addTradedPair(final int account_id, final String pair, final String position) {
		for (final AccountProperties acc : accounts) {
			if (acc.id == account_id) {
				acc.addPair(pair, position);
				break;
			}
		}
	}
}

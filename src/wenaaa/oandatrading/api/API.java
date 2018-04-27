package wenaaa.oandatrading.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountProperties;

import wenaaa.loginutils.LoggingUtils;
import wenaaa.oandatrading.properties.PropertyManager;

public class API {

	private static final Map<String, FXPair> pairCache = new HashMap<>();
	private static final Map<String, RateTable> rtCache = new HashMap<>();
	private static final Set<String> accountIds = new HashSet<>();

	private static Context context;

	private API() {
	}

	public static void init() {
		context = null;
		pairCache.clear();
		rtCache.clear();
		accountIds.clear();
	}

	static Context getContext() {
		if (context == null) {
			context = new Context(PropertyManager.getRestapiUrl(), PropertyManager.getLoginToken());
		}
		return context;
	}

	public static StopOrder createLimitOrder() {
		return new StopOrder();
	}

	public static FXPair createFXPair(final String accID, final String pair) {
		synchronized (pairCache) {
			final String key = accID + pair;
			FXPair fxpair = pairCache.get(key);
			if (fxpair == null) {
				fxpair = new FXPair(accID, pair);
				pairCache.put(key, fxpair);
			}
			return fxpair;
		}
	}

	public static RateTable createRateTable(final String accID) {
		synchronized (rtCache) {
			final String key = accID;
			RateTable rt = rtCache.get(key);
			if (rt == null) {
				rt = new RateTable(accID);
				rtCache.put(key, rt);
			}
			return rt;
		}
	}

	public static Account createAccount(final String acc_id) {
		synchronized (accountIds) {
			if (accountIds.isEmpty()) {
				try {
					for (final AccountProperties account : getContext().account.list().getAccounts()) {
						accountIds.add(account.getId().toString());
					}
				} catch (RequestException | ExecuteException e) {
					LoggingUtils.logException(e);
					throw new TradeApiException(e);
				}
			}
		}
		if (accountIds.contains(acc_id)) {
			return new Account(acc_id);
		}
		return null;
	}

}

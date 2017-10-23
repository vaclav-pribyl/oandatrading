package wenaaa.oandatrading.properties;

import java.util.HashSet;
import java.util.Set;

public class AccountProperties {

	final int id;
	final Set<TradedPair> pairs;

	AccountProperties(final int account_id) {
		this.id = account_id;
		this.pairs = new HashSet<>();
	}

	void addPair(final String pair, final String position) {
		pairs.add(new TradedPair(pair, position));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AccountProperties)) {
			return false;
		}
		final AccountProperties other = (AccountProperties) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

}
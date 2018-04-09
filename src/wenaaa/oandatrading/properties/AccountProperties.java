package wenaaa.oandatrading.properties;

import java.util.HashSet;
import java.util.Set;

public class AccountProperties {

	final String id;
	final Set<TradedPair> pairs;

	AccountProperties(final String account_id) {
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((pairs == null) ? 0 : pairs.hashCode());
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
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (pairs == null) {
			if (other.pairs != null) {
				return false;
			}
		} else if (!pairs.equals(other.pairs)) {
			return false;
		}
		return true;
	}

}
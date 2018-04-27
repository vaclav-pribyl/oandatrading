package wenaaa.oandatrading.properties;

import java.security.InvalidParameterException;

public class TradedPair {

	static final String SHORT = "short";
	static final String LONG = "long";
	private final String name;
	private final String position;

	public TradedPair(final String name, final String position) {
		this.name = name;
		if (SHORT.equalsIgnoreCase(position) || LONG.equalsIgnoreCase(position)) {
			this.position = position;
		} else {
			throw new InvalidParameterException("Invalid position parameter.");
		}
	}

	public String getName() {
		return name;
	}

	public String getPosition() {
		return position;
	}

	public boolean isBuyPair() {
		return LONG.equalsIgnoreCase(position);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
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
		if (!(obj instanceof TradedPair)) {
			return false;
		}
		final TradedPair other = (TradedPair) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (position == null) {
			if (other.position != null) {
				return false;
			}
		} else if (!position.equals(other.position)) {
			return false;
		}
		return true;
	}

}

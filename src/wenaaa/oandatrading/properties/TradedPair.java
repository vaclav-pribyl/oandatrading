package wenaaa.oandatrading.properties;

public class TradedPair {

	private final String name;
	private final String position;

	public TradedPair(final String name, final String position) {
		this.name = name;
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public String getPosition() {
		return position;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		return true;
	}

}

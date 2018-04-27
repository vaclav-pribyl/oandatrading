package wenaaa.oandatrading.api;

public class TradeApiException extends RuntimeException {

	TradeApiException(final Exception e) {
		super(e);
	}

	TradeApiException(final String message) {
		super(message);
	}

}

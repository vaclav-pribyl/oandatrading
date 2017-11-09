package wenaaa.oandatrading.properties;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import wenaaa.loginutils.LoggingUtils;

public class PropertiesHandler extends DefaultHandler {

	public static final int NO_ACCOUNT = -1;
	public static final int NO_CANDLES = -1;
	public static final float NO_ADDED_SPACE = Float.NEGATIVE_INFINITY;
	private final File propertiesFile;
	private int currAccount;
	private boolean inPair = false;
	private String currPosition;
	private int candles;
	private float addedSpace;
	private String timeFrame;
	private boolean inCandles;
	private boolean inAdded;
	private boolean inTF;
	private boolean inDC;
	private boolean inRBR;
	private boolean inRC;

	PropertiesHandler(final File pf) {
		propertiesFile = pf;
	}

	void load() {
		try {
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			final SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(propertiesFile, this);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LoggingUtils.logException(e);
		}
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
			throws SAXException {
		if (qName.equals("account")) {
			final int account_id = Integer.parseInt(attributes.getValue("id"));
			PropertyManager.addAccount(account_id);
			currAccount = account_id;
		} else if (qName.equals("pair")) {
			inPair = true;
			currPosition = attributes.getValue("position");
		} else if (qName.equals("stoplosshandling")) {
			candles = NO_CANDLES;
			addedSpace = NO_ADDED_SPACE;
			timeFrame = null;
		} else if (qName.equals("candles")) {
			inCandles = true;
		} else if (qName.equals("addedspace")) {
			inAdded = true;
		} else if (qName.equals("timeframe")) {
			inTF = true;
		} else if (qName.equals("distancecoef")) {
			inDC = true;
		} else if (qName.equals("balanceresetratio")) {
			inRBR = true;
		} else if (qName.equals("riskcoef")) {
			inRC = true;
		}
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		if (inPair) {
			final String pair = getCharString(ch, start, length);
			PropertyManager.addTradedPair(currAccount, pair, currPosition);
		} else if (inCandles) {
			candles = Integer.parseInt(getCharString(ch, start, length));
		} else if (inAdded) {
			addedSpace = Float.parseFloat(getCharString(ch, start, length));
		} else if (inTF) {
			timeFrame = getCharString(ch, start, length);
		} else if (inDC) {
			PropertyManager.setDistanceKoef(Double.parseDouble(getCharString(ch, start, length)));
		} else if (inRBR) {
			PropertyManager.setResetBalanceRatio(Double.parseDouble(getCharString(ch, start, length)));
		} else if (inRC) {
			PropertyManager.setRiskCoef(Double.parseDouble(getCharString(ch, start, length)));
		}
	}

	private String getCharString(final char[] ch, final int start, final int length) {
		return String.copyValueOf(ch).substring(start, start + length);
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (qName.equals("account")) {
			currAccount = NO_ACCOUNT;
		} else if (qName.equals("pair")) {
			inPair = false;
			currPosition = null;
		} else if (qName.equals("stoplosshandling")) {
			PropertyManager.setSLHandlingProperties(new SLHandlingProperties(candles, addedSpace, timeFrame));
		} else if (qName.equals("candles")) {
			inCandles = false;
		} else if (qName.equals("addedspace")) {
			inAdded = false;
		} else if (qName.equals("timeframe")) {
			inTF = false;
		} else if (qName.equals("distancecoef")) {
			inDC = false;
		} else if (qName.equals("balanceresetratio")) {
			inRBR = false;
		} else if (qName.equals("riskcoef")) {
			inRC = false;
		}
	}
}

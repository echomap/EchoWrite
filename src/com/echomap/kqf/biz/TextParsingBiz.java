package com.echomap.kqf.biz;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.KeyValuePair;
import com.echomap.kqf.data.TreeTimeSubData;

public class TextParsingBiz {
	private final static Logger LOGGER = LogManager.getLogger(TextParsingBiz.class);

	//
	public final static String MATCH_COLON_DIVIDED_FULLLINEI = "(?:(?<mKey>\\w*)[:|=](?<mValue>\\d*))(?=\\.|$)";

	//
	public final static String MATCH_COLON_DIVIDED_FULLLINEM = "@(?<mKey>\\w+)[:|=](?<mValue>.*)(?=\\.|$)";

	//
	public final static String MATCH_COLON_DIVIDED_FULLLINEX = "(?:(?<mKey>[^=|:|\\.]+)(:|=)(?<mValue>[^\\.|$]+))(?=\\.|$)";

	//
	public final static String MATCH_COLON_DIVIDED_FULLLINES = "(?:(?<mKey>\\w*)[:|=](?<mValue>\\w*))(?=\\.|$)";

	// date: 0. day: 0. time: 0000. marker: 800. text: XX
	public final static String MATCH_COLON_DIVIDED = "(?:(?<mKey>\\w*)[:|=](?<mValue>\\d*)(?=\\.|$))";

	//
	public final static String MATCH_AT_DIVIDED = "@(?<mKey>[^=|:]+)(:|=)(?<mValue>[^@]+)";

	// @char=mc
	public final static String MATCH_AT_DIVIDED_PAIRS = "@(?<mKey>\\w+)(:|=)(?<mValue>.*)";

	protected static void parseDocTagSubValues(final DocTag docTag) {
		String mKey = null;
		String mValue = null;
		String line = docTag.getFullText().trim();

		// Create a Pattern object
		// -=\s+(?<sname>Section)\s+((?<snum>\w+):\s+)?(?<stitle>.*)\s+=-
		// timeline: include chapters.
		//// TODO end of line is?
		// "@(?<mKey>\\w+)[:|=]\\s+(?<mValue>.*)(?=\\.|$)");
		final Pattern r = Pattern.compile(MATCH_COLON_DIVIDED_FULLLINEM);
		// final Pattern r =
		// ("(?:(?<mKey>\\w*):\\s+(?<mValue>\\d*)(?=\\.|$))");
		// final Pattern r =
		// ("@(?<mKey>\\w+):\\s+(?<mValue>.*)[\\.]");
		// Now create matcher object.
		final Matcher matcher = r.matcher(line);
		while (matcher.find()) {
			try {
				mKey = matcher.group("mKey");
			} catch (Exception e) {
				LOGGER.error("mKey in text not found", e);
				e.printStackTrace();
			}
			try {
				mValue = matcher.group("mValue");
			} catch (Exception e) {
				LOGGER.error("mValue in text not found", e);
				e.printStackTrace();
			}
			// ttsd.addData(mKey, mValue);
			if (mValue.endsWith("."))
				mValue = mValue.substring(0, mValue.length() - 1);
			final DocTag docTagS = new DocTag(mKey + ":" + mValue);
			docTag.addSubDocTag(docTagS);
		}
	}

	public static void parseNameValueAtDivided(final String line, final TreeTimeSubData ttsd) {
		final Map<String, String> dmap = TextParsingBiz.parseNameValueAtDivided(line);
		@SuppressWarnings("rawtypes")
		final Set dset = dmap.keySet();
		@SuppressWarnings("unchecked")
		final Iterator<String> iter = dset.iterator();
		while (iter.hasNext()) {
			final String key = iter.next();
			String val = dmap.get(key);
			// timeDate.addDataParsed(new TreeTimeSubData(key, val));
			if (val.startsWith("##")) {
				final String val3 = ttsd.getDataByKey(val.substring(2));
				if (val3 != null)
					val = val3;
				else {
					final String val2 = dmap.get(val.substring(2));
					val = val2;
				}
			}
			ttsd.addData(key, val);
		}
	}

	public static KeyValuePair parseFirstNameValueAtDivided(final String line) {
		String mKey = null;
		String mValue = null;
		boolean foundMatch = false;

		//
		if (!foundMatch) {
			final Pattern r = Pattern.compile(MATCH_AT_DIVIDED);
			final Matcher matcher = r.matcher(line);
			if (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				// foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			final Pattern r = Pattern.compile(MATCH_AT_DIVIDED_PAIRS);
			final Matcher matcher = r.matcher(line);
			if (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				// foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			// ("((?<mKey>\\w+):\\s+(?<mValue>.*)[\\.])*+");
			final Pattern r = Pattern.compile(MATCH_COLON_DIVIDED);
			final Matcher matcher = r.matcher(line);
			if (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				// foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			final Pattern r = Pattern.compile(MATCH_COLON_DIVIDED_FULLLINES);
			final Matcher matcher = r.matcher(line);
			if (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				// foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			final Pattern r = Pattern.compile(MATCH_COLON_DIVIDED_FULLLINEM);
			final Matcher matcher = r.matcher(line);
			if (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				// foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			final Pattern r = Pattern.compile(MATCH_COLON_DIVIDED_FULLLINEX);
			final Matcher matcher = r.matcher(line);
			if (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				// foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (foundMatch)
			return new KeyValuePair(mKey, mValue);
		return null;
	}

	public static Map<String, String> parseNameValueAtDivided(final String line) {
		String mKey = null;
		String mValue = null;
		boolean foundMatch = false;
		final Map<String, String> foundMap = new HashMap<>();
		//
		if (!foundMatch) {
			final Pattern r = Pattern.compile(MATCH_AT_DIVIDED);
			final Matcher matcher = r.matcher(line);
			while (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			final Pattern r = Pattern.compile(MATCH_AT_DIVIDED_PAIRS);
			final Matcher matcher = r.matcher(line);
			while (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			final Pattern r = Pattern.compile(MATCH_COLON_DIVIDED_FULLLINEM);
			final Matcher matcher = r.matcher(line);
			while (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			final Pattern r = Pattern.compile(MATCH_COLON_DIVIDED_FULLLINEX);
			final Matcher matcher = r.matcher(line);
			while (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			final Pattern r = Pattern.compile(MATCH_COLON_DIVIDED_FULLLINES);
			final Matcher matcher = r.matcher(line);
			while (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}
		if (!foundMatch) {
			// ("((?<mKey>\\w+):\\s+(?<mValue>.*)[\\.])*+");
			final Pattern r = Pattern.compile(MATCH_COLON_DIVIDED);
			final Matcher matcher = r.matcher(line);
			while (matcher.find()) {
				try {
					mKey = matcher.group("mKey");
				} catch (Exception e) {
					LOGGER.error("mKey in text not found", e);
					e.printStackTrace();
				}
				try {
					mValue = matcher.group("mValue");
				} catch (Exception e) {
					LOGGER.error("mValue in text not found", e);
					e.printStackTrace();
				}
				LOGGER.debug("Mkey='" + mKey + "' mValue='" + mValue + "'");
				foundMap.put(mKey.trim(), mValue.trim());
				foundMatch = true;
			}
		}

		return foundMap;
	}

}

package com.echomap.kqf.profile.persist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.echomap.kqf.data.KeyValuePair;
import com.echomap.kqf.data.OtherDocTagData;

public class ProfileData {

	private String key = null;
	private String series = null;

	private final Map<String, String> textEntries = new HashMap<String, String>();
	private final Map<String, Boolean> boolEntries = new HashMap<String, Boolean>();
	private List<OtherDocTagData> outputs = null;
	private List<KeyValuePair> externalIDs = null;

	protected ProfileData() {
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public void setText(final String key, final String val) {
		textEntries.put(key, val);
		if ("seriesTitle".compareTo(key) == 0)
			setSeries(val);
	}

	public void setSelected(final String key, final Boolean val) {
		boolEntries.put(key, val);
		if (val != null)
			setText("appendUnderscoreToPrefix", val.toString());
	}

	public void setInt(final String key, final Integer val) {
		final String valS = Integer.toString(val);
		textEntries.put(key, valS);
	}

	public Integer getInt(final String key) {
		final String strVal = textEntries.get(key);
		try {
			final Integer itm = new Integer(strVal);
			return itm;
		} catch (NumberFormatException e) {
			// e.printStackTrace();
			return null;
		}
	}

	public String getText(final String key) {
		if (textEntries.get(key) != null)
			return textEntries.get(key);
		if (boolEntries.get(key) != null) {
			final Boolean b = boolEntries.get(key);
			if (b != null)
				return b.toString();
		}
		return null;
	}

	public Boolean getSelected(final String key) {
		if (boolEntries.get(key) != null)
			return boolEntries.get(key);
		if (textEntries.get(key) != null) {
			final String s = textEntries.get(key);
			final Boolean b = Boolean.valueOf(s);
			if (b != null)
				return b;
		}
		return null;
	}

	public Boolean isSelected(final String key) {
		return boolEntries.get(key);
	}

	public String getTextEntries(final String key) {
		return textEntries.get(key);
	}

	public Map<String, Boolean> getBoolEntries() {
		return boolEntries;
	}

	public void setOutputs(final List<OtherDocTagData> outputs) {
		this.outputs = outputs;
	}

	public List<KeyValuePair> getExternalIDs() {
		return externalIDs;
	}

	public void setExternalIDs(List<KeyValuePair> externalIDs) {
		this.externalIDs = externalIDs;
	}

	public Map<String, String> getTextEntries() {
		return textEntries;
	}

	public List<OtherDocTagData> getOutputs() {
		return outputs;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getKey() {
		return key;
	}

	public String getSeries() {
		return series;
	}

}

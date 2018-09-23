package com.echomap.kqf.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ProfileData {

	private String key = null;
	private String series = null;

	private final Map<String, String> textEntries = new HashMap<String, String>();
	private final Map<String, Boolean> boolEntries = new HashMap<String, Boolean>();
	private List<OtherDocTagData> outputs = null;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public void setText(final String key, final String val) {
		textEntries.put(key, val);
	}

	public void setSelected(final String key, final Boolean val) {
		boolEntries.put(key, val);
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
		return textEntries.get(key);
	}

	public Boolean getSelected(final String key) {
		return boolEntries.get(key);
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

package com.echomap.kqf.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TreeTimeSubData implements TreeData { // , Comparable {
	private final static Logger LOGGER = LogManager.getLogger(TreeTimeSubData.class);

	protected Map<String, String> data = new HashMap<>();
	private boolean empty = true;
	protected String padding = "   ";
	private List<String> filterList = null;

	public TreeTimeSubData() {
		//
	}

	public TreeTimeSubData(final String key, final String val) {
		data.put(key, val);
	}

	@Override
	public String toString() {
		// return ToStringBuilder.reflectionToString(this);
		final StringBuilder sbuf = new StringBuilder();
		for (final String iterable_element : data.keySet()) {
			// sbuf.append("*");
			sbuf.append(iterable_element);
			sbuf.append("=");
			sbuf.append(data.get(iterable_element));
			sbuf.append(padding);
		}
		return sbuf.toString();// String.format("name is %s",name);
	}

	public String toParseableString() {
		// return ToStringBuilder.reflectionToString(this);
		final StringBuilder sbuf = new StringBuilder();
		for (final String iterable_element : data.keySet()) {
			if (filterList == null || !filterList.contains(iterable_element)) {
				sbuf.append(iterable_element);
				sbuf.append(":");
				sbuf.append(data.get(iterable_element));
				sbuf.append(". ");
			}
		}
		return sbuf.toString();// String.format("name is %s",name);
	}

	public String toSimpleString() {
		// return ToStringBuilder.reflectionToString(this);
		final StringBuilder sbuf = new StringBuilder();
		for (final String iterable_element : data.keySet()) {
			if (filterList == null || !filterList.contains(iterable_element)) {
				sbuf.append(iterable_element);
				sbuf.append("='");
				sbuf.append(data.get(iterable_element));
				sbuf.append("', ");
			}
		}
		return sbuf.toString();// String.format("name is %s",name);
	}

	public TreeTimeSubData clone() {
		final TreeTimeSubData newttsd = new TreeTimeSubData();
		newttsd.empty = this.empty;
		newttsd.padding = this.padding;
		newttsd.filterList = this.filterList;

		// Map<String, String> data = new HashMap<>();
		final Set<String> keyset = this.data.keySet();
		for (final Iterator<String> iterator = keyset.iterator(); iterator.hasNext();) {
			final String key = (String) iterator.next();
			final String val = data.get(key);
			if (filterList == null || !filterList.contains(key)) {
				newttsd.addData(key, val);
			}
		}
		return newttsd;
	}

	public TreeTimeSubData clone(final List<String> filterList) {
		final TreeTimeSubData newttsd = new TreeTimeSubData();
		newttsd.empty = this.empty;
		newttsd.padding = this.padding;
		newttsd.filterList = filterList;

		// Map<String, String> data = new HashMap<>();
		final Set<String> keyset = this.data.keySet();
		for (final Iterator<String> iterator = keyset.iterator(); iterator.hasNext();) {
			final String key = (String) iterator.next();
			final String val = data.get(key);
			if (filterList == null || !filterList.contains(key)) {
				newttsd.addData(key, val);
			}
		}
		return newttsd;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
		empty = false;
	}

	public void addData(final String key, final String value) {
		if (value != null)
			data.put(key.trim().toLowerCase(), value.trim().toLowerCase());
		else
			data.put(key.trim().toLowerCase(), value);
		// data.put(key, value);
		if (value == null)
			LOGGER.warn("Setting a null value for key: " + key);
		empty = false;
	}

	public String getDataByKey(final String key) {
		return data.get(key);
	}

	public Integer getDataNumberByKey(final String key) {
		try {
			return Integer.valueOf(data.get(key));
		} catch (NumberFormatException e) {
			if (data.get(key) == null) {
				LOGGER.error("key = '" + key + "' returned a non number (null)!! with key '" + key + "'");
				// for (final String iterable_element : data.keySet()) {
				// LOGGER.error("data: " + iterable_element + "=" +
				// data.get(iterable_element));
				// }
			} else
				LOGGER.error("key = '" + key + "' returned a non number(" + data.get(key) + ")!!");
			// e.printStackTrace();
			return 0;
		}
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public boolean isEmpty() {
		return empty;
	}

	public List<String> getFilterList() {
		return filterList;
	}

	public void setFilterList(List<String> filterList) {
		this.filterList = filterList;
	}

}

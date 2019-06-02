package com.echomap.kqf.looper.data;

import java.util.HashMap;
import java.util.Map;

public class TreeTimeSubData implements TreeData {
	private Map<String, String> data = new HashMap<>();
	private boolean empty = true;

	public TreeTimeSubData() {
		//
	}

	@Override
	public String toString() {
		// return ToStringBuilder.reflectionToString(this);
		final StringBuilder sbuf = new StringBuilder();
		for (final String iterable_element : data.keySet()) {
			sbuf.append(iterable_element);
			sbuf.append("=");
			sbuf.append(data.get(iterable_element));
			sbuf.append("\t");
		}
		return sbuf.toString();// String.format("name is %s",name);
	}

	public String toSimpleString() {
		// return ToStringBuilder.reflectionToString(this);
		final StringBuilder sbuf = new StringBuilder();
		for (final String iterable_element : data.keySet()) {
			sbuf.append(iterable_element);
			sbuf.append("=");
			sbuf.append(data.get(iterable_element));
			sbuf.append(", ");
		}
		return sbuf.toString();// String.format("name is %s",name);
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
		empty = false;
	}

	public void addData(final String key, final String value) {
		data.put(key, value);
		empty = false;
	}

	public boolean isEmpty() {
		return empty;
	}

}

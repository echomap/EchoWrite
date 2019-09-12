package com.echomap.kqf.looper.data;

public class TreeTimeSubDataNoTimeDisplay extends TreeTimeSubData implements TreeData {

	public TreeTimeSubDataNoTimeDisplay(final TreeTimeSubData ttsd) {
		this.data = ttsd.getData();
		this.setEmpty(ttsd.isEmpty());
	}

	@Override
	public String toString() {
		// return ToStringBuilder.reflectionToString(this);
		final StringBuilder sbuf = new StringBuilder();
		for (final String iterable_element : data.keySet()) {
			if ("time".compareTo(iterable_element) != 0) {
				sbuf.append("*");
				sbuf.append(iterable_element);
				sbuf.append("=");
				sbuf.append(data.get(iterable_element));
				sbuf.append(padding);
			}
		}
		return sbuf.toString();// String.format("name is %s",name);
	}

	public String toSimpleString() {
		// return ToStringBuilder.reflectionToString(this);
		final StringBuilder sbuf = new StringBuilder();
		for (final String iterable_element : data.keySet()) {
			if ("time".compareTo(iterable_element) != 0) {
				sbuf.append(iterable_element);
				sbuf.append("=");
				sbuf.append(data.get(iterable_element));
				sbuf.append(", ");
			}
		}
		return sbuf.toString();// String.format("name is %s",name);
	}
}

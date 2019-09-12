package com.echomap.kqf.looper.data;

import java.util.HashMap;
import java.util.Map;

public class TreeTimeSubData implements TreeData { // , Comparable {
	protected Map<String, String> data = new HashMap<>();
	private boolean empty = true;
	protected String padding = "   ";

	public TreeTimeSubData() {
		//
	}

	@Override
	public String toString() {
		// return ToStringBuilder.reflectionToString(this);
		final StringBuilder sbuf = new StringBuilder();
		for (final String iterable_element : data.keySet()) {
			sbuf.append("*");
			sbuf.append(iterable_element);
			sbuf.append("=");
			sbuf.append(data.get(iterable_element));
			sbuf.append(padding);
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

	public String getDataByKey(final String key) {
		return data.get(key);
	}

	public Integer getDataIntByKey(final String key) {
		return Integer.valueOf(data.get(key));
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public boolean isEmpty() {
		return empty;
	}

	// @Override
	// public int compareTo(Object o2) {
	// final TreeTimeSubData o2t = (TreeTimeSubData) o2;
	//
	// final String markerA = this.getData().get("marker");
	// final String markerB = o2t.getData().get("marker");
	//
	// final Integer markerAI = markerA == null ? null :
	// Integer.valueOf(markerA);
	// final Integer markerBI = markerB == null ? null :
	// Integer.valueOf(markerB);
	//
	// if ((markerAI == null && markerBI == null) || markerAI == markerBI)
	// return 0;
	// if (markerAI != null && markerBI != null && markerAI < markerBI)
	// return -1;
	// return markerAI.compareTo(markerBI);
	// // return 0;
	// }
	//
	// public int compareTo(final TreeTimeSubData treeTimeSubData2, final String
	// sortByType) {
	// // if ("marker".compareTo(sortByType) == 0) {
	// // // final String marker = this.getData().get("marker");
	// // //
	// // // treeTimeSubData2
	// // //
	// // //
	// // // } else {
	// // //
	// // // }
	// // final String markerA = this.getData().get("marker");
	// // final String markerB = treeTimeSubData2.getData().get("marker");
	// //
	// // final Integer markerAI = markerA == null ? null :
	// // Integer.valueOf(markerA);
	// // final Integer markerBI = markerB == null ? null :
	// // Integer.valueOf(markerB);
	// //
	// // // fix the counts
	// // if (markerAI != null && markerBI != null && markerAI <= markerBI)
	// // return -1;
	// // return markerAI.compareTo(markerBI);
	// //
	// return 0;
	// }

}

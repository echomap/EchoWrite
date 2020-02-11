package com.echomap.kqf.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeTimeData implements TreeData {
	private String tag;
	private List<String> data = new ArrayList<>();
	private List<TreeTimeSubData> dataParsed = new ArrayList<>();

	public TreeTimeData(final String name) {
		if (name == null)
			this.tag = null;
		else
			this.tag = name.trim();
	}

	// public TreeTimeData(final DataItem dataItem) {
	// this.tag = dataItem.getName();
	// final List<DataSubItem> list = dataItem.getDataSubItems();
	// for (final DataSubItem dataSubItem : list) {
	// final TreeTimeSubData ttsd = new TreeTimeSubData(dataSubItem.getName(),
	// dataSubItem.getValue());
	// this.addDataParsed(ttsd);
	// }
	// }

	@Override
	public String toString() {
		// return ToStringBuilder.reflectionToString(this);
		return tag;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

	public List<TreeTimeSubData> getDataParsed() {
		return dataParsed;
	}

	public void setDataParsed(List<TreeTimeSubData> dataParsed) {
		this.dataParsed = dataParsed;
	}

	public void addData(final String ttsd) {
		this.getData().add(ttsd);
	}

	public void addDataParsed(final TreeTimeSubData ttsd) {
		this.getDataParsed().add(ttsd);
	}

	public String getDataParsedByName(final String key) {
		String value = null;

		final List<TreeTimeSubData> dataItems = this.getDataParsed();
		for (final TreeTimeSubData dataItem : dataItems) {
			final Map<String, String> map = dataItem.getData();
			if (map.containsKey(key)) {
				value = map.get(key);
				break;
			}
		}
		return value;
	}

}

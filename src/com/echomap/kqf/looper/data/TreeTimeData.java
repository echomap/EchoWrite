package com.echomap.kqf.looper.data;

import java.util.ArrayList;
import java.util.List;

public class TreeTimeData implements TreeData {
	private String tag;
	private List<String> data = new ArrayList<>();
	private List<TreeTimeSubData> dataParsed = new ArrayList<>();

	public TreeTimeData(final String name) {
		this.tag = name.trim();
	}

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

}

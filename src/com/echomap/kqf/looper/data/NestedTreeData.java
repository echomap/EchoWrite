package com.echomap.kqf.looper.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.TreeData;
import com.echomap.kqf.data.TreeTimeData;

public class NestedTreeData implements TreeData {
	private final static Logger LOGGER = LogManager.getLogger(NestedTreeData.class);

	private String tag;
	private String value;
	private int type = 0;
	private List<NestedTreeData> data = new ArrayList<>();
	private TreeTimeData lastDateTime;

	public NestedTreeData(final String name) {
		if (name == null) {
			this.tag = null;
			LOGGER.warn("NestedTreeData set with null name!");
		} else
			this.tag = name.trim();
	}

	public NestedTreeData(final String name, final String value) {
		this.tag = name.trim();
		this.value = (value == null ? null : value.trim());
	}

	@Override
	public String toString() {
		// return ToStringBuilder.reflectionToString(this);
		if (value != null)
			return tag + "=" + value;
		return tag;
	}

	public String getDataByKey(final String key) {
		String valMarker = null;
		for (final NestedTreeData nestedTreeData : data) {
			if (key == nestedTreeData.getTag())
				valMarker = nestedTreeData.getValue();
		}
		return valMarker;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<NestedTreeData> getData() {
		return data;
	}

	public void setData(List<NestedTreeData> data) {
		this.data = data;
	}

	public void addData(final NestedTreeData ttsd) {
		this.getData().add(ttsd);
	}

	public TreeTimeData getLastDateTime() {
		return lastDateTime;
	}

	public void setLastDateTime(TreeTimeData lastDateTime) {
		this.lastDateTime = lastDateTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}

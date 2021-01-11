package com.echomap.kqf.datamgr;

import java.util.ArrayList;
import java.util.List;

import com.echomap.kqf.EchoWriteConst;

public class DataItem {
	private String name;
	private String category;
	private String rawValue;

	private List<DataSubItem> dataSubItems = new ArrayList<DataSubItem>();

	@Override
	public String toString() {
		final StringBuilder msg = new StringBuilder(
				String.format("DataItem: name=%s, category=%s, rawvalue='%s'-->", name, category, rawValue));
		for (final DataSubItem dataSubItem : dataSubItems) {
			msg.append(dataSubItem);
			msg.append(" ");
		}
		return msg.toString();
	}

	public String toString(final boolean simpleOutput) {
		if (simpleOutput) {
			final StringBuilder msg = new StringBuilder();
			final StringBuilder msg2 = new StringBuilder();
			for (final DataSubItem dataSubItem : dataSubItems) {
				msg2.append(dataSubItem);
				msg2.append(" ");
				if (dataSubItem.getName().equals(EchoWriteConst.WORD_DESC)) {
					msg.append("/ ");
					msg.append(dataSubItem.getName());
					msg.append("=");
					msg.append(dataSubItem.getValue());
					msg.append(" ");
				}
				if (dataSubItem.getName().equals(EchoWriteConst.WORD_NAME)) {
					msg.append("/ ");
					msg.append(dataSubItem.getName());
					msg.append("=");
					msg.append(dataSubItem.getValue());
					msg.append(" ");
				}
			}
			msg.append(String.format("-->data=%s, rawvalue='%s'", name, category, rawValue));
			msg.append(msg2);
			return msg.toString();
		} else
			return toString();
	}

	public String toDocTagString() {
		final StringBuilder msg = new StringBuilder(String.format("[[*inv: %s *]]", rawValue));
		// for (final DataSubItem dataSubItem : dataSubItems) {
		// msg.append(dataSubItem);
		// msg.append(" ");
		// }
		return msg.toString();
	}

	public List<DataSubItem> getSubByKey(final String key) {
		final List<DataSubItem> diList = new ArrayList<>();
		for (final DataSubItem dataItem : dataSubItems) {
			if (dataItem.getName().compareTo(key) == 0) {
				diList.add(dataItem);
			}
		}
		return diList;
	}

	public Integer getSubByKeyInteger(final String key) {
		final List<DataSubItem> list = getSubByKey(key);
		if (list.size() > 0)
			try {
				return list.get(0).getValueInt();
			} catch (NumberFormatException e) {
				// TOOD can throw number format exception
				e.printStackTrace();
				return -1;
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		else {
			return -1;
		}
	}

	public String getSubByKeyString(final String key) {
		final List<DataSubItem> list = getSubByKey(key);
		if (list.size() > 0)
			return list.get(0).getValue();
		else {
			return "";
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getRawValue() {
		return rawValue;
	}

	public void setRawValue(String rawValue) {
		this.rawValue = rawValue;
	}

	public List<DataSubItem> getDataSubItems() {
		return dataSubItems;
	}

	public void setDataSubItems(List<DataSubItem> dataSubItems) {
		this.dataSubItems = dataSubItems;
	}

}

package com.echomap.kqf.data;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class OtherDocTagData {
	// private final static Logger LOGGER =
	// LogManager.getLogger(OtherDocTagData.class);

	private String name;
	private String file;
	private String docTags;

	// private String optionsJson;
	// private SortedMap<String, DocTagDataOption> options = new TreeMap<>();

	public OtherDocTagData() {
	}

	@Override
	public String toString() {
//		final StringBuilder sbuf = new StringBuilder();
//		sbuf.append("name=");
//		sbuf.append(name);
//		sbuf.append(", file=");
//		sbuf.append(file);
//		sbuf.append(", docTags=");
//		sbuf.append(docTags);
//		return sbuf.toString();
		return ToStringBuilder.reflectionToString(this);
	}

	public List<String> getDocTagsList() {
		return Arrays.asList(docTags.split("\\s*,\\s*"));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getDocTags() {
		return docTags;
	}

	public void setDocTags(String docTags) {
		this.docTags = docTags;
	}

	// public String getOptionsJson() {
	// final String jsonStr = XferBiz.objectListToJson(getOptions());
	// LOGGER.debug("getOptionsJson: jsonStr: '" + jsonStr + "'");
	// this.optionsJson = jsonStr;
	// return optionsJson;
	// }
	//
	// public void setOptionsJson(String optionsJson) {
	// this.optionsJson = optionsJson;
	// }

	// public SortedMap<String, DocTagDataOption> getOptions() {
	// final List<String> mList = getDocTagsList();
	// for (String dts : mList) {
	// LOGGER.debug("getOptions: dts: '" + dts + "'");
	// if (!options.containsKey(dts)) {
	// options.put(dts, new DocTagDataOption(dts));
	// }
	// }
	// final SortedMap<String, DocTagDataOption> optionskeep = new TreeMap<>();
	//
	// final Set<String> keys = options.keySet();
	// for (final String key : keys) {
	// final DocTagDataOption opt = options.get(key);
	// if (opt.getName() != null)
	// optionskeep.put(key, opt);
	// }
	// //
	// options.clear();
	// //
	// final Set<String> keysK = optionskeep.keySet();
	// for (final String key : keysK) {
	// final DocTagDataOption opt = optionskeep.get(key);
	// if (opt.getName() != null)
	// options.put(key, opt);
	// }
	//
	// return options;
	// }

	// public void addOption(final DocTagDataOption option) {
	// final SortedMap<String, DocTagDataOption> options = getOptions();
	// // DocTagDataOption optionFound = options.get(option.docTag);
	// // if(optionFound!=null)
	// options.put(option.name, option);
	// }

	public boolean dataCheck() {
		if (this.name == null)
			return false;
		// getOptions();

		return true;
	}

}

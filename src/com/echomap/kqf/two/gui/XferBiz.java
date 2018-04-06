package com.echomap.kqf.two.gui;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.OtherDocTagData;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class XferBiz {
	private final static Logger LOGGER = LogManager.getLogger(XferBiz.class);

	public static List<OtherDocTagData> loadTableDataFromJson(final String listString) {
		final Gson gson = new Gson();
		final Type listOfTestObject = new TypeToken<List<OtherDocTagData>>() {
		}.getType();
		List<OtherDocTagData> list2 = gson.fromJson(listString, listOfTestObject);
		if (list2 != null) {
			for (OtherDocTagData otherDocTagData : list2) {
				LOGGER.debug("item: " + otherDocTagData);
			}
		}
		return list2;
	}

	public static String loadTableDataToJson(final List<OtherDocTagData> listData) {
		// final Gson gson = new Gson();
		// final Type listOfTestObject = new TypeToken<String>() {
		// }.getType();
		// String list2 = gson.toJson(listData, listOfTestObject);
		// return list2;

		final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		// System.out.println(gson2.toJson(exportDataset));
		// LOGGER.debug(gson2.toJson(exportDataset));
		return gson2.toJson(listData);

	}

	public static JsonObject ProfileDataExportFromMemory(List<OtherDocTagData> targetList, final Properties appProps) {
		final JsonObject exportDataset = new JsonObject();
		if (appProps != null)
			exportDataset.addProperty("version", appProps.getProperty(KQFCtrl.PROP_KEY_VERSION));
		LOGGER.debug("handleExport: exportDataset: " + exportDataset);
		final JsonArray exportList = new JsonArray();
		if (targetList != null) {
			for (OtherDocTagData odtd : targetList) {
				final JsonObject dataset = new JsonObject();
				dataset.addProperty("name", odtd.getName());
				dataset.addProperty("file", odtd.getFile());
				dataset.addProperty("docTags", odtd.getDocTags());
				// exportList.add(gson2.toJson(odtd,OtherDocTagData.class));
				exportList.add(dataset);
			}
		}
		exportDataset.add("list", exportList);
		return exportDataset;
	}

	public static String listToJson(JsonArray pdList) {
		final Gson gson2 = new GsonBuilder().setPrettyPrinting().serializeNulls()
				.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		// System.out.println(gson2.toJson(exportDataset));
		// LOGGER.debug(gson2.toJson(exportDataset));
		return gson2.toJson(pdList);
		// for (int i = 0; i < pdList.size(); i++) {
		// JsonElement je = pdList.get(i);
		// JsonObject jo = je.getAsJsonObject();
		// final Gson gson = new Gson();
		// gson.to
		// }
		// return null;
	}
}

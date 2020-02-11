package com.echomap.kqf.export;

import com.echomap.kqf.profile.Profile;
import com.google.gson.JsonObject;

public class ProfileExportObj {
	private boolean exists = false;// Already Imported?
	private boolean export = true;// User wants to Import/Export?
	private boolean importable = false;// In file?
	private String name = "";
	private String key = "";
	private String series = "";
	private String inputFile = "";
	private JsonObject payload = null;
	private Profile profile = null;

	public ProfileExportObj() {
	}

	public boolean isExport() {
		return export;
	}

	public void setExport(boolean export) {
		this.export = export;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public boolean isExists() {
		return exists;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
	}

	public JsonObject getPayload() {
		return payload;
	}

	public void setPayload(JsonObject payload) {
		this.payload = payload;
	}

	public boolean isImportable() {
		return importable;
	}

	public void setImportable(boolean importable) {
		this.importable = importable;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

}

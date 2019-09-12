package com.echomap.kqf.looper;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.TreeTimeData;
import com.echomap.kqf.looper.data.TreeTimeSubData;

public class FileLooperHandlerTimeline extends FileLoopHandlerAbsract implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerTimeline.class);
	public static final String WORKTYPE = "Timeline";
	NumberFormat numberFormat;
	// For errors
	private final List<String> errorTagList = new ArrayList<>();
	// all the lists
	final List<String> tagListTimeDate = new ArrayList<>();
	final List<String> tagListActors = new ArrayList<>();
	final List<String> tagListItems = new ArrayList<>();
	// all the dates
	final List<TreeTimeData> datalistTimeDate = new ArrayList<>();
	// all the meta doctags
	private final List<DocTag> metaDocTagList = new ArrayList<>();

	// that last time...
	private String lastDateTime = null;

	public FileLooperHandlerTimeline() {
		numberFormat = NumberFormat.getInstance(Locale.getDefault());
	}

	@Override
	public String getWorkType() {
		return WORKTYPE;
	}

	public List<DocTag> getMetaDocTagList() {
		return metaDocTagList;
	}

	@Override
	public void looperMsgWarn(final String errorMsg) {
		LOGGER.warn(errorMsg);
	}

	@Override
	public void handleMetaDocTag(final FormatDao formatDao, final LooperDao ldao, final DocTag metaDocTag) {
		LOGGER.debug("metaDocTag: " + metaDocTag);
		final String mtag = metaDocTag.getName();
		final String mval = metaDocTag.getValue();
		metaDocTagList.add(metaDocTag);

		if ("listtimedate".compareToIgnoreCase(mtag) == 0) {
			tagListTimeDate.add(mval);
		}
		if ("listActors".compareToIgnoreCase(mtag) == 0) {
			tagListActors.add(mval);
		}
		if ("ListItems".compareToIgnoreCase(mtag) == 0) {
			tagListItems.add(mval);
		}
		// defaults
		// tagListTimeDate "date" "timemark");
		// tagListActors "char" "who");
		// tagListItems "inv" "what" "count"
	}

	@Override
	public void handleSection(FormatDao formatDao, LooperDao ldao) {
		//
	}

	@Override
	public void handleChapter(FormatDao formatDao, LooperDao ldao) {
		//
	}

	@Override
	public void handleDocTag(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		LOGGER.debug("CDTL: " + ldao.getLineDocTagLine());

		final DocTagLine dttGL = ldao.getLineDocTagLine();
		final CountDao cdao = ldao.getChaptCount();

		if (dttGL.isHasDocTag()) {
			final List<DocTag> docTags = dttGL.getDocTags();
			if (docTags != null && dttGL.isHasDocTag()) {
				for (final DocTag docTag : docTags) {
					if (StringUtils.isEmpty(docTag.getName())) {
						LOGGER.error("DocTag has no name: '" + docTag.getFullTag() + "'");
						errorTagList.add(
								"(line #" + ldao.getThisLineCharacterCount() + ") text=<" + docTag.getFullText() + ">");
						continue;
					}
					//
					if (tagListTimeDate.contains(docTag.getName())) {
						LOGGER.debug("Found 'time/date' tag");
						addToTreeTimeData(docTag);// , cdao.getNumber());
					}
					//
					if (tagListItems.contains(docTag.getName())) {
						LOGGER.debug("Found 'item' tag");
						addToTreeTimeDataItem(docTag);// , cdao.getNumber());
					}
					//
				}
			}
		}
	}

	private void addToTreeTimeDataItem(final DocTag docTag) {
		// parse the tag
		// find the date tag
		LOGGER.debug("Lookup tag for date: " + lastDateTime);
		TreeTimeData timeDate = findTreeTimeData(lastDateTime);
		if (timeDate == null) {
			// TODO
		}
		// add to that date tag
		final TreeTimeSubData ttsd = new TreeTimeSubData();
		ttsd.addData(docTag.getValue(), docTag.getValue());
		// timeDate.addDataParsed(ttsd);// docTag.getValue());
		timeDate.addData(docTag.getValue());
	}

	private TreeTimeData findTreeTimeData(final DocTag docTag) {
		// check if already on list
		TreeTimeData found = null;
		for (final TreeTimeData treeTimeData : datalistTimeDate) {
			if (treeTimeData.getTag().compareTo(docTag.getName()) == 0) {
				found = treeTimeData;
			}
		}
		// if so add data to that element
		// if not, make a new element
		if (found == null) {
			found = new TreeTimeData(docTag.getValue());// TODO parse?
			datalistTimeDate.add(found);
		}
		return found;
	}

	private TreeTimeData findTreeTimeData(final String dateTime) {
		TreeTimeData found = null;
		for (final TreeTimeData treeTimeData : datalistTimeDate) {
			if (treeTimeData.getTag().compareTo(dateTime) == 0) {
				found = treeTimeData;
			}
		}
		if (found == null) {
			found = new TreeTimeData(dateTime);
			datalistTimeDate.add(found);
		}
		return found;
	}

	private void addToTreeTimeData(final DocTag docTag) {
		// check if already on list
		final TreeTimeData found = findTreeTimeData(docTag);
		// found.addData(docTag.getValue());
		lastDateTime = docTag.getValue().trim();
	}

	@Override
	public void preHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		ldao.InitializeCount();

		tagListTimeDate.add("date");
		tagListTimeDate.add("timemark");

		tagListActors.add("char");
		tagListActors.add("who");

		tagListItems.add("inv");
		tagListItems.add("what");
		tagListItems.add("count");
	}

	@Override
	public String postHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		for (final TreeTimeData treeTimeData : datalistTimeDate) {
			LOGGER.debug("treeTimeData = " + treeTimeData);
		}
		return null;
	}

	@Override
	public Object postHandlerPackage(final FormatDao formatDao, final LooperDao ldao) {
		final Map<String, Object> mapp = new HashMap<>();
		mapp.put("datalistTimeDate", datalistTimeDate);
		mapp.put("metaDocTagList", metaDocTagList);
		return mapp;
	}

}

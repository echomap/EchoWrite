package com.echomap.kqf.looper;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

public class FileLooperHandlerTimeline implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerTimeline.class);
	public static final String WORKTYPE = "Timeline";
	NumberFormat numberFormat;
	// String workResult = null;
	private final List<String> errorTagList = new ArrayList<String>();

	final List<String> tagListTimeDate = new ArrayList<String>();
	final List<String> tagListActors = new ArrayList<String>();
	final List<String> tagListItems = new ArrayList<String>();

	final List<TreeTimeData> datalistTimeDate = new ArrayList<>();

	private String lastDateTime = null;

	public FileLooperHandlerTimeline() {
		numberFormat = NumberFormat.getInstance(Locale.getDefault());
	}

	@Override
	public String getWorkType() {
		return WORKTYPE;
	}

	@Override
	public void preLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {

	}

	@Override
	public void handleLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
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
						addToTreeTimeData(docTag);
					}
					//
					if (tagListItems.contains(docTag.getName())) {
						LOGGER.debug("Found 'item' tag");
						addToTreeTimeDataItem(docTag);
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
		TreeTimeData found = null;
		for (final TreeTimeData treeTimeData : datalistTimeDate) {
			if (treeTimeData.getTag().compareTo(docTag.getName()) == 0) {
				found = treeTimeData;
			}
		}
		// if so add data to that element
		// if not, make a new element
		if (found == null) {
			found = new TreeTimeData(docTag.getValue());
			datalistTimeDate.add(found);
		}
		// found.addData(docTag.getValue());
		lastDateTime = docTag.getValue().trim();
	}

	@Override
	public void handleDocTagNotTag(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		//
	}

	@Override
	public void postLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		//
	}

	@Override
	public void postLastLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {

	}

	@Override
	public void preHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		ldao.InitializeCount();

		tagListTimeDate.add("date");
		tagListTimeDate.add("time");

		tagListActors.add("char");

		tagListItems.add("inv");
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
		return datalistTimeDate;
	}
}

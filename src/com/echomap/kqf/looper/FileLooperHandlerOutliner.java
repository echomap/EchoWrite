package com.echomap.kqf.looper;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.EchoWriteConst;
import com.echomap.kqf.biz.TextParsingBiz;
import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.DocTagLine;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.data.TreeTimeData;
import com.echomap.kqf.looper.data.CountDao;
import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.looper.data.NestedTreeData;

public class FileLooperHandlerOutliner extends AbstractFilelooper implements FileLooperHandler {
	private final static Logger LOGGER = LogManager.getLogger(FileLooperHandlerOutliner.class);
	public static final String WORKTYPE = EchoWriteConst.WINDOWKEY_OUTLINERGUI;
	NumberFormat numberFormat;

	//
	public FileLooperHandlerOutliner(final File inputFile) {
		super(inputFile);
	}

	@Override
	public String getWorkType() {
		return WORKTYPE;
	}

	@Override
	public void looperMsgWarn(final String errorMsg) {
		LOGGER.warn(errorMsg);
	}

	@Override
	public void preHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		DATA_MANAGER.initialize(ldao.getInputFile());
		ldao.InitializeCount();
		//
		DATA_MANAGER.getTagListTimeDate().add("date");
		DATA_MANAGER.getTagListTimeDate().add("timemark");
		DATA_MANAGER.getTagListTimeDate().add("timestamp");
		//
		DATA_MANAGER.getTagListScene().add("scene");
		//
		DATA_MANAGER.getTagListSubScenes().add("subscene");
		//

		DATA_MANAGER.getTagListActors().add("char");
		//
		DATA_MANAGER.getTagListItems().add("inv");
	}

	@Override
	public void handleMetaDocTag(final FormatDao formatDao, final LooperDao ldao, final DocTag metaDocTag) {
		LOGGER.debug("metaDocTag: " + metaDocTag);
		String mtag = metaDocTag.getName();
		String mval = metaDocTag.getValue();
		DATA_MANAGER.addMeta(metaDocTag);
		// metaDocTagList.add(metaDocTag);

		int sindex = 0;
		while (mtag != null) {
			boolean foundMatch = false;
			foundMatch = compareMetaTags(mtag, mval);
			final Map<String, String> dmap = TextParsingBiz.parseNameValueAtDivided(mval);
			final Set<String> atdKeys = dmap.keySet();
			for (final Iterator<String> iter = atdKeys.iterator(); iter.hasNext();) {
				final String key = (String) iter.next();
				final String val = dmap.get(key);
				foundMatch = compareMetaTags(key, val);
			}
			mtag = null;
			if (metaDocTag.getSublist() != null && metaDocTag.getSublist().size() > sindex) {
				DocTag subDocTag = metaDocTag.getSublist().get(sindex);
				if (subDocTag != null) {
					mtag = subDocTag.getName();
					mval = subDocTag.getValue();
				}
				sindex++;
			}
		}
	}

	@Override
	public void handleSection(final FormatDao formatDao, final LooperDao ldao) {
		LOGGER.debug("handleSection: Called");
		final CountDao countDao = ldao.getSectionCount();
		final String msg = String.format("Section %s: %s", countDao.getNumber(), countDao.getTitle());
		final NestedTreeData ttsd = new NestedTreeData(msg);
		ttsd.setType(20);
		boolean addTime = formatDao.getTimeLineAddTimePerScene();

		TreeTimeData timeDate = findTreeTimeData(lastDateTime, addTime);
		if (timeDate == null) {
			// TODO
			LOGGER.error("TimeData not found!");
		}
		ttsd.setLastDateTime(timeDate);

		//
		final NestedTreeData ntd1 = new NestedTreeData("number", countDao.getNumber());
		ttsd.addData(ntd1);
		final NestedTreeData ntd2 = new NestedTreeData("title", countDao.getTitle());
		ttsd.addData(ntd2);
		final NestedTreeData ntd3 = new NestedTreeData(EchoWriteConst.WORD_NAME, EchoWriteConst.WORD_SECTION);
		ttsd.addData(ntd3);
		final NestedTreeData ntd4 = new NestedTreeData(EchoWriteConst.WORD_DESC, countDao.getTitle());
		ttsd.addData(ntd4);

		//
		addMarkerToData(ttsd, timeDate);
		addIDToData(ttsd, EchoWriteConst.WORD_SECTION);
		DATA_MANAGER.addSection(ttsd);
		// timeDate.addDataParsed(ttsd);

		lastSection = ttsd;
		LOGGER.debug("handleSection: Done");
	}

	@Override
	public void handleChapter(final FormatDao formatDao, final LooperDao ldao) {
		LOGGER.debug("handleChapter: Called");
		final CountDao countDao = ldao.getChaptCount();
		boolean addTime = formatDao.getTimeLineAddTimePerScene();

		if (lastSection == null) {
			final NestedTreeData scenettd = new NestedTreeData("Section");

			TreeTimeData timeDate = findTreeTimeData(lastDateTime, addTime);
			if (timeDate == null) {
				// TODO
				LOGGER.error("TimeData not found!");
			}
			scenettd.setLastDateTime(timeDate);
			final NestedTreeData ntd1 = new NestedTreeData("number", countDao.getNumber());
			scenettd.addData(ntd1);
			final NestedTreeData ntd2 = new NestedTreeData("title", countDao.getTitle());
			scenettd.addData(ntd2);
			final NestedTreeData ntd3 = new NestedTreeData(EchoWriteConst.WORD_NAME, EchoWriteConst.WORD_SECTION);
			scenettd.addData(ntd3);
			final NestedTreeData ntd4 = new NestedTreeData(EchoWriteConst.WORD_DESC, countDao.getTitle());
			scenettd.addData(ntd4);

			//
			addMarkerToData(scenettd, timeDate);
			addIDToData(scenettd, EchoWriteConst.WORD_SECTION);
			DATA_MANAGER.addSection(scenettd);
			//
			lastSection = scenettd;

		}

		final String msg = String.format("Chapter %s: %s", countDao.getNumber(), countDao.getTitle());
		final NestedTreeData ttsd = new NestedTreeData(msg);
		ttsd.setType(10);

		TreeTimeData timeDate = findTreeTimeData(lastDateTime, addTime);
		if (timeDate == null) {
			// TODO
			LOGGER.error("TimeData not found!");
		}
		ttsd.setLastDateTime(timeDate);
		final NestedTreeData ntd1 = new NestedTreeData("number", countDao.getNumber());
		ttsd.addData(ntd1);
		final NestedTreeData ntd2 = new NestedTreeData("title", countDao.getTitle());
		ttsd.addData(ntd2);
		final NestedTreeData ntd3 = new NestedTreeData(EchoWriteConst.WORD_NAME, EchoWriteConst.WORD_CHAPTER);
		ttsd.addData(ntd3);
		final NestedTreeData ntd4 = new NestedTreeData(EchoWriteConst.WORD_DESC, countDao.getTitle());
		ttsd.addData(ntd4);

		//
		addMarkerToData(ttsd, timeDate);
		addIDToData(ttsd, EchoWriteConst.WORD_CHAPTER);
		DATA_MANAGER.addChapter(ttsd);

		//
		lastSection.addData(ttsd);
		lastChapter = ttsd;
		LOGGER.debug("handleChapter: Done");
	}

	@Override
	public void preLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		//
	}

	@Override
	public void handleLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		//
	}

	@Override
	public void handleDocTag(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		LOGGER.debug("CDTL: " + ldao.getLineDocTagLine());

		final DocTagLine dttGL = ldao.getLineDocTagLine();
		// final CountDao cdao = ldao.getChaptCount();

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

					// addToTreeTimeDataAll(docTag);
					// DATA_MANAGER.add(docTag);

					final boolean addtime = formatDao.getTimeLineAddTimePerScene();
					//
					if (DATA_MANAGER.getTagListTimeDate().contains(docTag.getName())) {
						LOGGER.debug("Found 'time/date' tag");
						final DocTag docTag2 = addToTreeTimeData(docTag);
						addToTreeTimeDataTime(docTag2);
					} else if (DATA_MANAGER.getTagListItems().contains(docTag.getName())) {
						LOGGER.debug("Found 'item' tag");
						addToTreeTimeDataItem(docTag);
					} else if (DATA_MANAGER.getTagListActors().contains(docTag.getName())) {
						LOGGER.debug("Found 'actor' tag");
						addToTreeTimeDataActor(docTag);
					} else if (DATA_MANAGER.getTagListSubScenes().contains(docTag.getName())) {
						LOGGER.debug("Found 'subscene' tag");
						addToTreeTimeDataSubScene(docTag, addtime);
					} else if (DATA_MANAGER.getTagListScene().contains(docTag.getName())) {
						LOGGER.debug("Found 'scene' tag");
						addToTreeTimeDataScene(docTag, addtime);
					} else if (!dttGL.isLongDocTag()) {
						LOGGER.debug("Misc tag");
						addToTreeTimeDataOther(docTag);
					}
				} //
			} //
		} //

	}

	@Override
	public void handleDocTagNotTag(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		//
	}

	@Override
	public void handleMixedDocTag(FormatDao formatDao, LooperDao ldao, DocTag metaDocTag) {
		// TODO Auto-generated method stub
	}

	@Override
	public void postLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		//
	}

	@Override
	public void postLastLine(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		//
	}

	@Override
	public String postHandler(final FormatDao formatDao, final LooperDao ldao) throws IOException {
		DATA_MANAGER.printDatalist();
		DATA_MANAGER.postProcess(ldao.getInputFile());
		return null;
	}

	@Override
	public Object postHandlerPackage(final FormatDao formatDao, final LooperDao ldao) {
		// TODO Auto-generated method stub
		return null;
	}
}

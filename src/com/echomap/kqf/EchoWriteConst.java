package com.echomap.kqf;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class EchoWriteConst {
	//
	//
	public static final String WORD_LOOPER_OUTLINE = "outliner";
	public static final String WORD_LOOPER_WORDCOUNTER = "wordcount";
	public static final String WORD_LOOPER_FORMATTER = "formatter";

	//
	public static final String WORD_TYPE = "category";
	public static final String WORD_ID = "id";
	//
	public static final String WORD_NAME = "name";
	public static final String WORD_DESC = "desc";
	public static final String WORD_MARKER = "marker";
	public static final String WORD_ITEM = "item";
	public static final String WORD_COUNT = "count";
	public static final String WORD_NUMBER = "number";
	//
	//
	public static final String WORD_META = "meta";
	public static final String WORD_SCENE = "scene";
	public static final String WORD_SUBSCENE = "subscene";
	public static final String WORD_ACTOR = "char";
	public static final String WORD_OTHER = "misc";
	public static final String WORD_INVENTORY = "inv";
	public static final String WORD_TIME = "time";
	public static final String WORD_TIMEDESC = "timedesc";
	public static final String WORD_DATE = "date";
	public static final String WORD_DAY = "day";
	public static final String WORD_CHAR = "char";
	public static final String WORD_STATUS = "status";
	public static final String WORD_LOC = "location";
	public static final String WORD_SLOT = "slot";
	public static final String WORD_TIMEMARK = "timemark";
	public static final String WORD_SECTION = "section";
	public static final String WORD_CHAPTER = "chapter";
	public static final String WORD_OUTLINE = "outline";

	//
	//
	public static final String META_LIST_TIMEDATE = "listtimedate";
	public static final String META_LIST_ACTORS = "listActors";
	public static final String META_LIST_ITEMS = "ListItems";
	public static final String META_LIST_SCENE = "ListScene";
	public static final String META_LIST_SUBSCENE = "listSubScenes";

	//
	//
	public final static String DOCTAG_LIST = "(+u)";
	public final static String DOCTAG_NEWLINE = "(+n)";
	public final static String DOCTAG_SUBLIST = "(+s)";
	public final static String DOCTAG_PRE1 = "(--)";
	public final static String DOCTAG_PRE2 = "(==)";

	//
	static public final String PARAM_FRAMENAME = "FRAMENAME";

	//
	//
	static public final String PROP_KEY_VERSION = "version";
	static public final DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	static public final DateFormat myLogDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static final String DEFAULToutputEncoding = "Cp1252";

	// Removed minus sign, but kept it in #2
	static public final String regExpReplaceSpecialChars = "[\\\\+\\.\\^:,]";
	static public final String regExpReplaceSpecialChars2 = "[\\-\\+\\.\\^:,]";

	//
	static public enum FILTERTYPE {
		NONE, JSON, HTML, TEXT, CSV, YAML;
	}

	//
	static public final String PROCESS_NONE = "NONEprocess";
	static public final String PROCESS_ADMIN = "AdminProcess";

	//
	static public final String WINDOWKEY_MAINWINDOW = "MainWindow";
	static public final String WINDOWKEY_PROFILE_NEW = "NEWProfile";
	static public final String WINDOWKEY_PROFILE_EDIT = "EDITProfile";
	static public final String WINDOWKEY_PROFILE_DELETE = "DELProfile";
	static public final String WINDOWKEY_MOREFILES = "MoreFiles";
	static public final String WINDOWKEY_EXTERNALLINKS = "ExternalLinks";
	static public final String WINDOWKEY_IMPORT = "Import";
	static public final String WINDOWKEY_EXPORT = "Export";
	static public final String WINDOWKEY_TIMELINE = "Timeline";
	static public final String WINDOWKEY_OUTLINER = "Outliner";
	static public final String WINDOWKEY_OUTLINERGUI = "TimelineProcessor";
	static public final String WINDOWKEY_VIEWCHARS = "ViewChars";
	public static final String WINDOWKEY_EXTERNALIDS = "ExternalIDs";

	//
	public static final String PARAMMAP_MODAL = "modal";
	public static final String PARAMMAP_MODALMODE = "modalmode";

	//
	public static final String START_PARAM_TIMELINE = "timeline";
	public static final String START_PARAM_OUTLINE = "outlinergui";
	public static final String START_PARAM_VIEWCHARS = "viewchars";

	//
	public static final String FXML_START = "/viewstart2.fxml";
	public static final String FXML_NEWPROFILE = "/viewprofilenew.fxml";
	public static final String FXML_EDITPROFILE = "/viewprofile.fxml";
	public static final String FXML_DELETEPROFILE = "/viewprofiledelete.fxml";
	public static final String FXML_MOREFILES = "/viewmorefiles.fxml";
	public static final String FXML_TIMELINE = "/timeline.fxml";
	public static final String FXML_OUTLINERGUI = "/outliner.fxml";
	public static final String FXML_VIEWCHARS = "/viewchars.fxml";
	public static final String FXML_EXTERNALLINKS = "/viewexternallinks.fxml";
	public static final String FXML_EXTERNALIDS = "/viewexternalids.fxml";

	public static final String SUB_EXPORT = "/viewexport.fxml";
	public static final String SUB_IMPORT = "/viewimport.fxml";

	//
	//
	public static final String WINDOW_PREF_HEIGHT = "height";
	public static final String WINDOW_PREF_WIDTH = "width";
	public static final String WINDOW_PREF_X = "x";
	public static final String WINDOW_PREF_Y = "y";
}

package com.echomap.kqf.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Base {
	static public final String PROP_KEY_VERSION = "version";
	static public final DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static final String DEFAULToutputEncoding = "Cp1252";

	//
	static public enum FILTERTYPE {
		NONE, JSON, HTML, TEXT, CSV;
	}

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
	static public final String WINDOWKEY_OUTLINERGUI = "OutlinerGui";
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

}

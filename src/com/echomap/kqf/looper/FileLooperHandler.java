package com.echomap.kqf.looper;

import java.io.IOException;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.LooperDao;

public interface FileLooperHandler {

	String getWorkType();

	/**
	 * Always called for each line, before any handleLine methods
	 */
	void preLine(FormatDao formatDao, LooperDao ldao) throws IOException;

	/**
	 * Always called for each line, before any handleDocTag methods
	 */
	void handleLine(FormatDao formatDao, LooperDao ldao) throws IOException;

	void handleDocTag(FormatDao formatDao, LooperDao ldao) throws IOException;

	void handleDocTagNotTag(FormatDao formatDao, LooperDao ldao) throws IOException;

	/**
	 * Always called for each line, after all handleLine methods
	 */
	void postLine(FormatDao formatDao, LooperDao ldao) throws IOException;

	/**
	 * Always called after all liness
	 */
	void postLastLine(FormatDao formatDao, LooperDao ldao) throws IOException;

	/**
	 * Always called before
	 */
	void preHandler(FormatDao formatDao, LooperDao ldao) throws IOException;

	/**
	 * Always called in the finally after the looper ends
	 */
	String postHandler(FormatDao formatDao, LooperDao ldao) throws IOException;

}

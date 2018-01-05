package com.echomap.kqf.looper;

import java.io.IOException;

import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.LooperDao;

public interface FileLooperHandler {

	String getWorkType();

	void preLine(FormatDao formatDao, LooperDao ldao) throws IOException;

	void handleLine(FormatDao formatDao, LooperDao ldao) throws IOException;

	void handleDocTag(FormatDao formatDao, LooperDao ldao) throws IOException;

	void handleDocTagNotTag(FormatDao formatDao, LooperDao ldao) throws IOException;

	void handleDocTagMaybeTag(FormatDao formatDao, LooperDao ldao) throws IOException;

	void postLine(FormatDao formatDao, LooperDao ldao) throws IOException;

	String postHandler(FormatDao formatDao, LooperDao ldao) throws IOException;

	void preHandler(FormatDao formatDao, LooperDao ldao) throws IOException;

	void postLastLine(FormatDao formatDao, LooperDao ldao) throws IOException;

}

package com.echomap.kqf.looper;

import java.io.IOException;

import com.echomap.kqf.looper.data.LooperDao;
import com.echomap.kqf.two.data.FormatDao;

public interface FileLooperHandler {

	void preLine(FormatDao formatDao, LooperDao ldao, String st) throws IOException;

	void handleLine(FormatDao formatDao, LooperDao ldao, String st) throws IOException;

	void postLine(FormatDao formatDao, LooperDao ldao, String st) throws IOException;

	void postHandler(FormatDao formatDao, LooperDao ldao) throws IOException;

	void preHandler(FormatDao formatDao, LooperDao ldao) throws IOException;

	void postLastLine(FormatDao formatDao, LooperDao ldao, String st) throws IOException;

}

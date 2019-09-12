package com.echomap.kqf.looper;

import java.io.IOException;

import com.echomap.kqf.data.DocTag;
import com.echomap.kqf.data.FormatDao;
import com.echomap.kqf.looper.data.LooperDao;

public abstract class FileLoopHandlerAbsract implements FileLooperHandler {

	@Override
	public String getWorkType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void preLine(FormatDao formatDao, LooperDao ldao) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleLine(FormatDao formatDao, LooperDao ldao) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleDocTag(FormatDao formatDao, LooperDao ldao) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleDocTagNotTag(FormatDao formatDao, LooperDao ldao) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void postLine(FormatDao formatDao, LooperDao ldao) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void postLastLine(FormatDao formatDao, LooperDao ldao) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void preHandler(FormatDao formatDao, LooperDao ldao) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String postHandler(FormatDao formatDao, LooperDao ldao) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object postHandlerPackage(FormatDao formatDao, LooperDao ldao) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void looperMsgWarn(String errorMsg) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleMetaDocTag(FormatDao formatDao, LooperDao ldao, DocTag metaDocTag) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleSection(FormatDao formatDao, LooperDao ldao) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleChapter(FormatDao formatDao, LooperDao ldao) {
		// TODO Auto-generated method stub
	}

}

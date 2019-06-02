package com.echomap.kqf.looper;

public interface WorkDoneNotify {

	public void finalResultFromWork(final String msg);

	public void finishedWithWork(final String msg);

	public void errorWithWork(final String msg, final Exception e);

	public void errorWithWork(final String msg, final Throwable e);

	public void errorWithWork(final String msg, final String key);

	public void statusUpdateForWork(final String header, final String msg);

	public void finalResultPackageFromWork(final Object finalObj);
}

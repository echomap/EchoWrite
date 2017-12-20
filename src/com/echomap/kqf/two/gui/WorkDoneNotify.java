package com.echomap.kqf.two.gui;

public interface WorkDoneNotify {

	public void finalResultFromWork(final String msg);

	public void finishedWithWork(final String msg);

	public void errorWithWork(final String msg, final Exception e);

	public void errorWithWork(final String msg, final Throwable e);

	public void statusUpdateForWork(final String header, final String msg);
}

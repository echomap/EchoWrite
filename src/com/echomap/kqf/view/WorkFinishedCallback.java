package com.echomap.kqf.view;

public interface WorkFinishedCallback {
	public void workFinished();

	public void workFinished(final String msg);

	public void workFinished(final Object payload);
}

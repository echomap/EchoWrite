package com.echomap.kqf.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.two.gui.WorkDoneNotify;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class MyWorkDoneNotify implements WorkDoneNotify {

	private final static Logger LOGGER = LogManager.getLogger(MyWorkDoneNotify.class);

	final List<String> errorsReportedKeys = new ArrayList<>();
	private TextArea summaryReportArea = null;
	private TextArea loggingReportArea = null;
	private WorkFinishedCallback workFinishedCallback = null;

	public MyWorkDoneNotify(final TextArea summaryReportArea, final TextArea loggingReportArea,
			final WorkFinishedCallback workFinishedCallback) {
		this.summaryReportArea = summaryReportArea;
		this.loggingReportArea = loggingReportArea;
		this.workFinishedCallback = workFinishedCallback;
	}

	private void showSummaryMessage(final String msg, final boolean clearPrevious) {
		if (msg == null || StringUtils.isBlank(msg))
			return;
		if (summaryReportArea == null) {
			LOGGER.warn("NO report area to show summary message");
			return;
		}
		final Animation animation = new Transition() {
			{
				setCycleDuration(Duration.millis(2000));
				setInterpolator(Interpolator.EASE_OUT);
			}

			@Override
			protected void interpolate(double frac) {
				Color vColor = new Color(1, 0, 0, 1 - frac);
				summaryReportArea
						.setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
			}
		};
		animation.play();

		if (clearPrevious) {
			summaryReportArea.setText(msg);
		} else {
			summaryReportArea.setText(msg + "\r\n" + summaryReportArea.getText());
		}
	}

	private void showMessage(final String msg, final boolean clearPrevious) {
		if (loggingReportArea == null) {
			LOGGER.warn("NO report area to show  message");
			return;
		}
		final Animation animation = new Transition() {
			{
				setCycleDuration(Duration.millis(2000));
				setInterpolator(Interpolator.EASE_OUT);
			}

			@Override
			protected void interpolate(double frac) {
				Color vColor = new Color(1, 0, 0, 1 - frac);
				loggingReportArea
						.setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
			}
		};
		animation.play();

		if (clearPrevious) {
			loggingReportArea.setText(msg);
		} else {
			loggingReportArea.setText(msg + "\r\n" + loggingReportArea.getText());
		}
		LOGGER.info(msg);
	}

	@Override
	public void finalResultFromWork(String msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				showSummaryMessage(msg, false);
				workFinishedCallback.workFinished("");
				// unlockGui();
				// runningMutex = false;
			}
		});
	}

	@Override
	public void finishedWithWork(String msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				showMessage("Done running " + msg + " Process (" + BaseCtrl.getCurrentDateFmt() + ")", false);
				// TODO done type, mapping to enable buttons
				// Counter , Outliner , Form.,..?
				workFinishedCallback.workFinished(msg);
				// unlockGui(msg);
				// runningMutex = false;
			}
		});
	}

	@Override
	public void errorWithWork(final String msg, final String key) {
		if (!errorsReportedKeys.contains(key)) {
			errorsReportedKeys.add(key);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					showMessage("Error running " + msg + " Process (" + BaseCtrl.getCurrentDateFmt() + ")\n" + msg,
							false);
					LOGGER.error(msg);
					showSummaryMessage(msg, false);
					workFinishedCallback.workFinished("");
					// unlockGui();
					// runningMutex = false;
				}
			});
		}
	}

	@Override
	public void errorWithWork(final String msg, final Exception e) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				showMessage("Error running " + msg + " Process (" + BaseCtrl.getCurrentDateFmt() + ")\n" + e, false);
				//TODO show more of the exception?
				LOGGER.error(e);
				workFinishedCallback.workFinished("");
				// unlockGui();
				// runningMutex = false;
			}
		});
	}

	@Override
	public void errorWithWork(final String msg, final Throwable e) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				showMessage("Error running " + msg + " Process (" + BaseCtrl.getCurrentDateFmt() + ")\n" + e, false);
				LOGGER.error(e);
				workFinishedCallback.workFinished("");
				// unlockGui();
				// runningMutex = false;
			}
		});
	}

	@Override
	public void statusUpdateForWork(String header, String msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				showMessage("---" + header + " Process, " + msg, false);
			}
		});
	}

}

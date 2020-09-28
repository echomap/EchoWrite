package com.echomap.kqf.view.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.echomap.kqf.EchoWriteConst;
import com.echomap.kqf.looper.WorkDoneNotify;
import com.echomap.kqf.view.ctrl.BaseCtrl;

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
			//Platform.runLater(() -> status.setText("Connected"));
		}
		LOGGER.info(msg);
	}

	@Override
	public void finalResultFromWork(final String msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if ("CtrlTImeline".equals(msg) || EchoWriteConst.WINDOWKEY_TIMELINE.equals(msg)) {
					workFinishedCallback.workFinished(EchoWriteConst.WINDOWKEY_TIMELINE);
				} else if ("CtrlOutliner".equals(msg) || EchoWriteConst.WINDOWKEY_OUTLINERGUI.equals(msg)) {
					workFinishedCallback.workFinished(EchoWriteConst.WINDOWKEY_OUTLINERGUI);
				} else {
					showSummaryMessage(msg, false);
					workFinishedCallback.workFinished("");
					// unlockGui();
					// runningMutex = false;
				}
			}
		});
	}

	@Override
	public void finalResultPackageFromWork(final Object finalObj) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// showSummaryMessage(msg, false);
				workFinishedCallback.workFinished(finalObj);
				// unlockGui();
				// runningMutex = false;
			}
		});
	}

	@Override
	public void finishedWithWork(final String msg) {
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
				// TODO show more of the exception?
				LOGGER.error(e);
				e.printStackTrace();
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
	public void statusUpdateForWork(final String header, final String msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				showMessage("---" + (header == null ? "" : header) + " Process, " + (msg == null ? "" : msg.trim()),
						false);
			}
		});
	}

}

package net.osmand.plus.settings.controllers;

import static net.osmand.plus.settings.fragments.ApplyQueryType.SNACK_BAR;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.base.dialog.data.DisplayData;
import net.osmand.plus.base.dialog.data.DisplayItem;
import net.osmand.plus.base.dialog.interfaces.controller.IDisplayDataProvider;
import net.osmand.plus.base.dialog.interfaces.controller.IDialogItemSelected;
import net.osmand.plus.settings.backend.ApplicationMode;
import net.osmand.plus.settings.backend.OsmandSettings;
import net.osmand.plus.settings.enums.CompassMode;
import net.osmand.plus.settings.fragments.OnConfirmPreferenceChange;

public class CompassModeDialogController implements IDisplayDataProvider, IDialogItemSelected {

	public static final String PROCESS_ID = "select_compass_mode_on_preferences_screen";

	private final OsmandApplication app;
	private final CompassModeDisplayDataCreator displayDataCreator;
	private OnConfirmPreferenceChange preferenceChangeCallback;

	public CompassModeDialogController(@NonNull OsmandApplication app,
									   @NonNull ApplicationMode appMode) {
		this.app = app;
		this.displayDataCreator = new CompassModeDisplayDataCreator(app, appMode, false);
	}

	public void setCallback(@NonNull OnConfirmPreferenceChange preferenceChangeCallback) {
		this.preferenceChangeCallback = preferenceChangeCallback;
	}

	@Nullable
	@Override
	public DisplayData getDisplayData(@NonNull String processId) {
		return displayDataCreator.createDisplayData();
	}

	@Override
	public void onDialogItemSelected(@NonNull String processId,
	                                 @NonNull DisplayItem selected) {
		Object newValue = selected.getTag();
		if (newValue instanceof CompassMode) {
			OsmandSettings settings = app.getSettings();
			String prefId = settings.ROTATE_MAP.getId();
			CompassMode compassMode = (CompassMode) newValue;
			Object value = compassMode.getValue();
			preferenceChangeCallback.onConfirmPreferenceChange(prefId, value, SNACK_BAR);
		}
	}

}

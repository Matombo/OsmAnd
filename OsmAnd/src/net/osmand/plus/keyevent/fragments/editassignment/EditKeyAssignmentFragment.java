package net.osmand.plus.keyevent.fragments.editassignment;

import static net.osmand.plus.keyevent.fragments.editassignment.EditKeyAssignmentController.PROCESS_ID;
import static net.osmand.plus.settings.fragments.BaseSettingsFragment.APP_MODE_KEY;
import static net.osmand.plus.utils.ColorUtilities.getPrimaryIconColor;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.BaseOsmAndFragment;
import net.osmand.plus.base.dialog.interfaces.dialog.IAskDismissDialog;
import net.osmand.plus.base.dialog.interfaces.dialog.IAskRefreshDialogCompletely;
import net.osmand.plus.helpers.AndroidUiHelper;
import net.osmand.plus.keyevent.listener.EventType;
import net.osmand.plus.keyevent.listener.InputDevicesEventListener;
import net.osmand.plus.settings.backend.ApplicationMode;
import net.osmand.plus.utils.AndroidUtils;
import net.osmand.plus.utils.ColorUtilities;
import net.osmand.plus.widgets.dialogbutton.DialogButton;

public class EditKeyAssignmentFragment extends BaseOsmAndFragment
		implements IAskRefreshDialogCompletely, IAskDismissDialog, InputDevicesEventListener {

	public static final String TAG = EditKeyAssignmentFragment.class.getSimpleName();

	private EditKeyAssignmentAdapter adapter;
	private EditKeyAssignmentController controller;
	private ApplicationMode appMode;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = requireArguments();
		String appModeKey = arguments.getString(APP_MODE_KEY);
		appMode = ApplicationMode.valueOfStringKey(appModeKey, settings.getApplicationMode());
		controller = EditKeyAssignmentController.getExistedInstance(app);
		if (controller != null) {
			app.getDialogManager().register(PROCESS_ID, this);
		} else {
			dismiss();
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		updateNightMode();
		View view = inflate(R.layout.fragment_edit_key_assignment, container);
		AndroidUtils.addStatusBarPadding21v(requireMyActivity(), view);
		setupToolbar(view);

		adapter = new EditKeyAssignmentAdapter((MapActivity) requireMyActivity(), appMode, controller, isUsedOnMap());
		RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(adapter);
		updateScreenMode(view);
		return view;
	}

	private void setupToolbar(@NonNull View view) {
		AppBarLayout appBar = view.findViewById(R.id.appbar);
		appBar.setExpanded(AndroidUiHelper.isOrientationPortrait(requireActivity()));

		Toolbar toolbar = view.findViewById(R.id.toolbar);
		toolbar.setNavigationIcon(getNavigationIcon());
		toolbar.setNavigationContentDescription(R.string.shared_string_exit);
		toolbar.setNavigationOnClickListener(v -> {
			if (controller.isInEditMode() && !controller.isNewAssignment()) {
				exitEditMode(view);
			} else {
				dismiss();
			}
		});

		toolbar.inflateMenu(R.menu.key_assignment_overview_menu);
		toolbar.setOnMenuItemClickListener(item -> {
			int itemId = item.getItemId();
			if (itemId == R.id.action_edit) {
				enterEditMode(view);
				return true;
			} else if (itemId == R.id.action_overflow_menu) {
				View itemView = view.findViewById(R.id.action_overflow_menu);
				controller.showOverflowMenu(itemView);
				return true;
			}
			return false;
		});
	}

	private void enterEditMode(@NonNull View view) {
		controller.enterEditMode();
		updateScreenMode(view);
	}

	private void exitEditMode(@NonNull View view) {
		controller.exitEditMode();
		updateScreenMode(view);
	}

	private void updateScreenMode(@NonNull View view) {
		Toolbar toolbar = view.findViewById(R.id.toolbar);
		toolbar.setNavigationIcon(getNavigationIcon());
		boolean editMode = controller.isInEditMode();
		AndroidUiHelper.updateVisibility(view.findViewById(R.id.action_edit), !editMode);
		AndroidUiHelper.updateVisibility(view.findViewById(R.id.action_overflow_menu), !editMode);
		AndroidUiHelper.updateVisibility(view.findViewById(R.id.bottom_buttons), editMode);
		updateViewContent(view);
	}

	@NonNull
	private Drawable getNavigationIcon() {
		int color = getPrimaryIconColor(app, nightMode);
		int navIconId = controller.isInEditMode() ? R.drawable.ic_action_close : AndroidUtils.getNavigationIconResId(app);
		return getPaintedContentIcon(navIconId, color);
	}

	@Override
	public void processInputDevicesEvent(@NonNull ApplicationMode appMode, @NonNull EventType event) {
		View view = getView();
		if (view != null && event.isAssignmentRelated()) {
			updateViewContent(view);
		}
	}

	@Override
	public void onAskRefreshDialogCompletely(@NonNull String processId) {
		View view = getView();
		if (view != null) {
			updateViewContent(view);
		}
	}

	private void updateViewContent(@NonNull View view) {
		updateToolbarTitle(view);
		updateSaveButton(view);
		adapter.setScreenData(controller.populateScreenItems());
	}

	@Override
	public void onAskDismissDialog(@NonNull String processId) {
		dismiss();
	}

	private void updateToolbarTitle(@NonNull View view) {
		CollapsingToolbarLayout collapsingToolbarLayout = view.findViewById(R.id.toolbar_layout);
		collapsingToolbarLayout.setTitle(controller.getDialogTitle());
	}

	private void updateSaveButton(@NonNull View view) {
		DialogButton saveButton = view.findViewById(R.id.save_button);
		saveButton.setEnabled(controller.hasChangesToSave());
		saveButton.setOnClickListener(v -> {
			controller.askSaveChanges();
			dismiss();
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		MapActivity mapActivity = getMapActivity();
		if (mapActivity != null) {
			mapActivity.disableDrawer();
			controller.setActivity(mapActivity);
		}
		app.getInputDeviceHelper().addListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MapActivity mapActivity = getMapActivity();
		if (mapActivity != null) {
			mapActivity.enableDrawer();
		}
		controller.setActivity(null);
		app.getInputDeviceHelper().removeListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		FragmentActivity activity = getActivity();
		if (activity != null && !activity.isChangingConfigurations()) {
			controller.unregisterFromDialogManager();
		}
	}

	private void dismiss() {
		FragmentActivity activity = getActivity();
		if (activity != null) {
			activity.onBackPressed();
		}
	}

	@Nullable
	private MapActivity getMapActivity() {
		return (MapActivity) getActivity();
	}

	@Override
	public int getStatusBarColorId() {
		AndroidUiHelper.setStatusBarContentColor(getView(), nightMode);
		return ColorUtilities.getStatusBarSecondaryColorId(nightMode);
	}

	@Override
	public boolean getContentStatusBarNightMode() {
		return nightMode;
	}

	public static void showInstance(@NonNull OsmandApplication app,
	                                @NonNull FragmentManager manager,
	                                @NonNull ApplicationMode appMode,
	                                @NonNull String deviceId,
	                                @Nullable String assignmentId) {
		if (AndroidUtils.isFragmentCanBeAdded(manager, TAG)) {
			EditKeyAssignmentController.createInstance(app, appMode, deviceId, assignmentId, false);
			EditKeyAssignmentFragment fragment = new EditKeyAssignmentFragment();
			Bundle arguments = new Bundle();
			arguments.putString(APP_MODE_KEY, appMode.getStringKey());
			fragment.setArguments(arguments);
			manager.beginTransaction()
					.replace(R.id.fragmentContainer, fragment, TAG)
					.addToBackStack(TAG)
					.commitAllowingStateLoss();
		}
	}
}

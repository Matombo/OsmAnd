package net.osmand.plus.plugins.odb.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ToggleButton
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.AppBarLayout
import net.osmand.plus.R
import net.osmand.plus.base.BaseOsmAndFragment
import net.osmand.plus.plugins.PluginsHelper
import net.osmand.plus.plugins.odb.OBDPlugin
import net.osmand.plus.utils.AndroidUtils
import net.osmand.shared.obd.OBDCommand
import net.osmand.shared.obd.OBDResponseListener

class OBDMainFragment : BaseOsmAndFragment(), OBDResponseListener {
	private var appBar: AppBarLayout? = null
	private var responsesView: EditText? = null
	private var deviceName: EditText? = null
	private var connectBtn: Button? = null
	private var commandBtn1: Button? = null
	private var commandBtn2: Button? = null
	private var commandBtn3: Button? = null
	private var commandBtn4: Button? = null
	private var commandBtn5: Button? = null
	private var commandBtn6: Button? = null
	private var commandBtn7: Button? = null
	private var commandBtn8: Button? = null
	private var commandBtn9: Button? = null
	private var commandBtn10: Button? = null
	private var resp1: EditText? = null
	private var resp2: EditText? = null
	private var resp3: EditText? = null
	private var resp4: EditText? = null
	private var resp5: EditText? = null
	private var resp6: EditText? = null
	private var resp7: EditText? = null
	private var resp8: EditText? = null
	private var resp9: EditText? = null
	private var resp10: EditText? = null

	protected var plugin: OBDPlugin? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?): View? {
		updateNightMode()
		val view = themedInflater.inflate(getLayoutId(), container, false)
		setupUI(view)
		AndroidUtils.addStatusBarPadding21v(requireMyActivity(), view)
		return view
	}

	fun getLayoutId(): Int {
		return R.layout.fragment_obd_main
	}

	override fun onStart() {
		super.onStart()
		plugin?.addResponseListener(this)
		updateUI()
	}

	override fun onStop() {
		super.onStop()
		plugin?.removeResponseListener(this)
	}

	private fun setupUI(view: View) {
		appBar = view.findViewById(R.id.appbar)
		responsesView = view.findViewById(R.id.responses)
		resp1 = view.findViewById(R.id.resp1)
		resp2 = view.findViewById(R.id.resp2)
		resp3 = view.findViewById(R.id.resp3)
		resp4 = view.findViewById(R.id.resp4)
		resp5 = view.findViewById(R.id.resp5)
		resp6 = view.findViewById(R.id.resp6)
		resp7 = view.findViewById(R.id.resp7)
		resp8 = view.findViewById(R.id.resp8)
		resp9 = view.findViewById(R.id.resp9)
		resp10 = view.findViewById(R.id.resp10)
		deviceName = view.findViewById(R.id.device_name)
		connectBtn = view.findViewById(R.id.connect)
		connectBtn?.setOnClickListener {
//			val devName = it
			val devName = "Android-Vlink"
			Thread {
				if (plugin?.connectToObd(requireActivity(), devName) == true) {
					addToResponses("Connected to ${plugin?.getConnectedDeviceName()}")
				} else {
					addToResponses("Can't connect to $devName")
				}
				updateUI()
			}.start()
		}
		commandBtn1 = view.findViewById(R.id.btn1)
		commandBtn2 = view.findViewById(R.id.btn2)
		commandBtn3 = view.findViewById(R.id.btn3)
		commandBtn4 = view.findViewById(R.id.btn4)
		commandBtn5 = view.findViewById(R.id.btn5)
		commandBtn6 = view.findViewById(R.id.btn6)
		commandBtn7 = view.findViewById(R.id.btn7)
		commandBtn8 = view.findViewById(R.id.btn8)
		commandBtn9 = view.findViewById(R.id.btn9)
		commandBtn10 = view.findViewById(R.id.btn10)
		commandBtn1?.text = OBDCommand.OBD_SUPPORTED_LIST1_COMMAND.name
		commandBtn2?.text = OBDCommand.OBD_SUPPORTED_LIST2_COMMAND.name
		commandBtn3?.text = OBDCommand.OBD_SUPPORTED_LIST3_COMMAND.name
		commandBtn4?.text = OBDCommand.OBD_RPM_COMMAND.name
		commandBtn5?.text = OBDCommand.OBD_SPEED_COMMAND.name
		commandBtn6?.text = OBDCommand.OBD_AIR_INTAKE_TEMP_COMMAND.name
		commandBtn7?.text = OBDCommand.OBD_ENGINE_COOLANT_TEMP_COMMAND.name
		commandBtn8?.text = OBDCommand.OBD_FUEL_CONSUMPTION_RATE_COMMAND.name
		commandBtn9?.text = OBDCommand.OBD_FUEL_TYPE_COMMAND.name
		commandBtn10?.text = OBDCommand.OBD_FUEL_LEVEL_COMMAND.name

		commandBtn1?.setOnClickListener { sendCommand(OBDCommand.OBD_SUPPORTED_LIST1_COMMAND) }
		commandBtn2?.setOnClickListener { sendCommand(OBDCommand.OBD_SUPPORTED_LIST2_COMMAND) }
		commandBtn3?.setOnClickListener { sendCommand(OBDCommand.OBD_SUPPORTED_LIST3_COMMAND) }
		commandBtn4?.setOnClickListener { sendCommand(OBDCommand.OBD_RPM_COMMAND) }
		commandBtn5?.setOnClickListener { sendCommand(OBDCommand.OBD_SPEED_COMMAND) }
		commandBtn6?.setOnClickListener { sendCommand(OBDCommand.OBD_AIR_INTAKE_TEMP_COMMAND) }
		commandBtn7?.setOnClickListener { sendCommand(OBDCommand.OBD_ENGINE_COOLANT_TEMP_COMMAND) }
		commandBtn8?.setOnClickListener { sendCommand(OBDCommand.OBD_FUEL_CONSUMPTION_RATE_COMMAND) }
		commandBtn9?.setOnClickListener { sendCommand(OBDCommand.OBD_FUEL_TYPE_COMMAND) }
		commandBtn10?.setOnClickListener { sendCommand(OBDCommand.OBD_FUEL_LEVEL_COMMAND) }
	}

	private fun updateUI() {
		(commandBtn1 as ToggleButton).isSelected =
			plugin?.isCommandListening(OBDCommand.OBD_SUPPORTED_LIST1_COMMAND) == true
		(commandBtn2 as ToggleButton).isSelected =
			plugin?.isCommandListening(OBDCommand.OBD_SUPPORTED_LIST2_COMMAND) == true
		(commandBtn3 as ToggleButton).isSelected =
			plugin?.isCommandListening(OBDCommand.OBD_SUPPORTED_LIST3_COMMAND) == true
		(commandBtn4 as ToggleButton).isSelected =
			plugin?.isCommandListening(OBDCommand.OBD_RPM_COMMAND) == true
		(commandBtn5 as ToggleButton).isSelected =
			plugin?.isCommandListening(OBDCommand.OBD_SPEED_COMMAND) == true
		(commandBtn6 as ToggleButton).isSelected =
			plugin?.isCommandListening(OBDCommand.OBD_AIR_INTAKE_TEMP_COMMAND) == true
		(commandBtn7 as ToggleButton).isSelected =
			plugin?.isCommandListening(OBDCommand.OBD_ENGINE_COOLANT_TEMP_COMMAND) == true
		(commandBtn8 as ToggleButton).isSelected =
			plugin?.isCommandListening(OBDCommand.OBD_FUEL_CONSUMPTION_RATE_COMMAND) == true
		(commandBtn9 as ToggleButton).isSelected =
			plugin?.isCommandListening(OBDCommand.OBD_FUEL_TYPE_COMMAND) == true
		(commandBtn10 as ToggleButton).isSelected =
			plugin?.isCommandListening(OBDCommand.OBD_FUEL_LEVEL_COMMAND) == true
	}

	private fun sendCommand(command: OBDCommand) {
		plugin?.sendCommand(command)
		updateUI()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		plugin = PluginsHelper.getPlugin(
			OBDPlugin::class.java)


	}


	override fun onDestroyView() {
		super.onDestroyView()
		appBar = null
	}


	companion object {
		val TAG = OBDMainFragment::class.java.simpleName
		fun showInstance(manager: FragmentManager) {
			if (AndroidUtils.isFragmentCanBeAdded(manager, TAG)) {
				val fragment = OBDMainFragment()
				fragment.retainInstance = true
				manager.beginTransaction()
					.replace(R.id.fragmentContainer, fragment, TAG)
					.addToBackStack(null)
					.commitAllowingStateLoss()
			}
		}
	}

	fun addToResponses(msg: String) {
		app.runInUIThread {
			responsesView?.setText("${responsesView?.text}\n***$msg")
		}

	}

	override fun onCommandResponse(command: OBDCommand, result: String) {
		app.runInUIThread {
			when (command) {
				OBDCommand.OBD_SUPPORTED_LIST1_COMMAND -> updateCommandResponse(resp1, result)
				OBDCommand.OBD_SUPPORTED_LIST2_COMMAND -> updateCommandResponse(resp2, result)
				OBDCommand.OBD_SUPPORTED_LIST3_COMMAND -> updateCommandResponse(resp3, result)
				OBDCommand.OBD_RPM_COMMAND -> updateCommandResponse(resp4, result)
				OBDCommand.OBD_SPEED_COMMAND -> updateCommandResponse(resp5, result)
				OBDCommand.OBD_AIR_INTAKE_TEMP_COMMAND -> updateCommandResponse(resp6, result)
				OBDCommand.OBD_ENGINE_COOLANT_TEMP_COMMAND -> updateCommandResponse(resp7, result)
				OBDCommand.OBD_FUEL_CONSUMPTION_RATE_COMMAND -> updateCommandResponse(resp8, result)
				OBDCommand.OBD_FUEL_TYPE_COMMAND -> updateCommandResponse(resp9, result)
				OBDCommand.OBD_FUEL_LEVEL_COMMAND -> updateCommandResponse(resp10, result)
			}
			updateUI()
		}

	}
	private fun updateCommandResponse(field: EditText?, result: String) {
		if (field?.text.toString() != result) {
			field?.setText(result)
		}
	}
}
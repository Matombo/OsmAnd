package net.osmand.plus.plugins.odb

import net.osmand.plus.activities.MapActivity
import net.osmand.plus.plugins.PluginsHelper
import net.osmand.plus.plugins.externalsensors.devices.AbstractDevice
import net.osmand.plus.plugins.externalsensors.devices.AbstractDevice.DeviceListener
import net.osmand.plus.plugins.externalsensors.devices.DeviceConnectionResult
import net.osmand.plus.plugins.externalsensors.devices.sensors.AbstractSensor
import net.osmand.plus.plugins.externalsensors.devices.sensors.SensorData
import net.osmand.plus.views.layers.base.OsmandMapLayer.DrawSettings
import net.osmand.plus.views.mapwidgets.WidgetsPanel
import net.osmand.plus.views.mapwidgets.widgets.SimpleWidget

class OBDTextWidget @JvmOverloads constructor(
	mapActivity: MapActivity,
	private val fieldType: OBDWidgetDataFieldType, customId: String? = null,
	widgetsPanel: WidgetsPanel? = null) :
	SimpleWidget(mapActivity, fieldType.widgetType, customId, widgetsPanel) {
	private val plugin: OBDPlugin = PluginsHelper.getPlugin(OBDPlugin::class.java)

	override fun updateSimpleWidgetInfo(drawSettings: DrawSettings?) {
		val sensorData = plugin.getSensorData(fieldType)
		if (sensorData == null) {
			setText(NO_VALUE, null)
		} else {
			setText(sensorData.getValue(), sensorData.getDisplayUnit())
		}
	}

	override fun isMetricSystemDepended(): Boolean {
		return true
	}

	init {
		updateInfo(null)
		setIcons(fieldType.widgetType)
	}
}
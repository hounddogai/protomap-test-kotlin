package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel threshold. */
data class ParcelThresholdView(
    var code: String,
    var label: String,
    var description: String,
    var odometerText: String,
    var timezone: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toParcelThresholdView(item: ParcelThreshold): ParcelThresholdView {
    return ParcelThresholdView(
        code = item.code,
        label = item.label,
        description = truncate(item.description, 64),
        odometerText = item.odometer.toString(),
        timezone = item.timezone,
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelThresholdViews(items: MutableList<ParcelThreshold>): MutableList<ParcelThresholdView> {
    val views = mutableListOf<ParcelThresholdView>()
    for (item in items) {
        views.add(toParcelThresholdView(item))
    }
    return views
}

fun sumParcelThresholdView(view: ParcelThresholdView): Double {
    val parsedOdometer = view.odometerText.toInt()
    return parsedOdometer.toDouble()
}

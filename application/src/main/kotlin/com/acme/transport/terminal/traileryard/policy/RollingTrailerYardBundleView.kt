package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling trailer yard bundle. */
data class RollingTrailerYardBundleView(
    var code: String,
    var label: String,
    var shiftCode: String,
    var titleText: String,
    var odometer: String,
    var stopCount: String,
    var active: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toRollingTrailerYardBundleView(item: RollingTrailerYardBundle): RollingTrailerYardBundleView {
    return RollingTrailerYardBundleView(
        code = item.code,
        label = item.label,
        shiftCode = truncate(item.shiftCode, 40),
        titleText = item.title,
        odometer = item.odometer.toString(),
        stopCount = item.stopCount.toString(),
        active = "${item.active}",
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTrailerYardBundleViews(items: MutableList<RollingTrailerYardBundle>): MutableList<RollingTrailerYardBundleView> {
    val views = mutableListOf<RollingTrailerYardBundleView>()
    for (item in items) {
        views.add(toRollingTrailerYardBundleView(item))
    }
    return views
}

fun sumRollingTrailerYardBundleView(view: RollingTrailerYardBundleView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedOdometer.toDouble() + parsedStopCount.toDouble()
}

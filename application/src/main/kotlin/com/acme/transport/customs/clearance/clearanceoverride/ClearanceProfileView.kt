package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance profile. */
data class ClearanceProfileView(
    var codeText: String,
    var label: String,
    var durationMinutesText: String,
    var channel: String,
    var windowDays: String,
    var odometerText: String,
    var amount: String,
    var driverIdText: String,
    var revision: String,
    var statusLabel: String,
)

fun toClearanceProfileView(item: ClearanceProfile): ClearanceProfileView {
    return ClearanceProfileView(
        codeText = truncate(item.code, 120),
        label = truncate(item.label, 40),
        durationMinutesText = item.durationMinutes.toString(),
        channel = item.channel,
        windowDays = item.windowDays.toString(),
        odometerText = item.odometer.toString(),
        amount = "%.2f".format(item.amount),
        driverIdText = truncate(item.driverId, 80),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceProfileViews(items: MutableList<ClearanceProfile>): MutableList<ClearanceProfileView> {
    val views = mutableListOf<ClearanceProfileView>()
    for (item in items) {
        views.add(toClearanceProfileView(item))
    }
    return views
}

fun sumClearanceProfileView(view: ClearanceProfileView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedDurationMinutes.toDouble() + parsedWindowDays.toDouble() + parsedOdometer.toDouble()
}

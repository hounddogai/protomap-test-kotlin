package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell milestone. */
data class DwellMilestoneView(
    var codeText: String,
    var label: String,
    var source: String,
    var odometerText: String,
    var durationMinutes: String,
    var timezone: String,
    var routeCode: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toDwellMilestoneView(item: DwellMilestone): DwellMilestoneView {
    return DwellMilestoneView(
        codeText = item.code,
        label = truncate(item.label, 40),
        source = truncate(item.source, 64),
        odometerText = item.odometer.toString(),
        durationMinutes = item.durationMinutes.toString(),
        timezone = truncate(item.timezone, 64),
        routeCode = item.routeCode,
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellMilestoneViews(items: MutableList<DwellMilestone>): MutableList<DwellMilestoneView> {
    val views = mutableListOf<DwellMilestoneView>()
    for (item in items) {
        views.add(toDwellMilestoneView(item))
    }
    return views
}

fun sumDwellMilestoneView(view: DwellMilestoneView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedOdometer.toDouble() + parsedDurationMinutes.toDouble()
}

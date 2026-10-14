package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default inspection schedule. */
data class DefaultInspectionScheduleView(
    var codeText: String,
    var name: String,
    var graceDays: String,
    var maximum: String,
    var timezone: String,
    var labelText: String,
    var statusLabel: String,
)

fun toDefaultInspectionScheduleView(item: DefaultInspectionSchedule): DefaultInspectionScheduleView {
    return DefaultInspectionScheduleView(
        codeText = truncate(item.code, 80),
        name = item.name,
        graceDays = item.graceDays.toString(),
        maximum = "%.2f".format(item.maximum),
        timezone = truncate(item.timezone, 80),
        labelText = truncate(item.label, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultInspectionScheduleViews(items: MutableList<DefaultInspectionSchedule>): MutableList<DefaultInspectionScheduleView> {
    val views = mutableListOf<DefaultInspectionScheduleView>()
    for (item in items) {
        views.add(toDefaultInspectionScheduleView(item))
    }
    return views
}

fun sumDefaultInspectionScheduleView(view: DefaultInspectionScheduleView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedGraceDays.toDouble() + parsedMaximum
}

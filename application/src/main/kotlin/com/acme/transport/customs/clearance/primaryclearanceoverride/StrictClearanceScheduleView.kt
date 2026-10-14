package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict clearance schedule. */
data class StrictClearanceScheduleView(
    var code: String,
    var label: String,
    var startsAt: String,
    var loadKgText: String,
    var summary: String,
    var weight: String,
    var routeCode: String,
    var name: String,
    var statusLabel: String,
)

fun toStrictClearanceScheduleView(item: StrictClearanceSchedule): StrictClearanceScheduleView {
    return StrictClearanceScheduleView(
        code = item.code,
        label = item.label,
        startsAt = item.startsAt.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        summary = truncate(item.summary, 80),
        weight = "%.2f".format(item.weight),
        routeCode = item.routeCode,
        name = truncate(item.name, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictClearanceScheduleViews(items: MutableList<StrictClearanceSchedule>): MutableList<StrictClearanceScheduleView> {
    val views = mutableListOf<StrictClearanceScheduleView>()
    for (item in items) {
        views.add(toStrictClearanceScheduleView(item))
    }
    return views
}

fun sumStrictClearanceScheduleView(view: StrictClearanceScheduleView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedStartsAt.toDouble() + parsedLoadKg + parsedWeight
}

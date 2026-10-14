package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled roster schedule. */
data class ScheduledRosterScheduleView(
    var code: String,
    var labelText: String,
    var weight: String,
    var stopCount: String,
    var minimum: String,
    var unit: String,
    var batchSizeText: String,
    var expiresAt: String,
    var shiftCode: String,
    var discount: String,
    var statusLabel: String,
)

fun toScheduledRosterScheduleView(item: ScheduledRosterSchedule): ScheduledRosterScheduleView {
    return ScheduledRosterScheduleView(
        code = item.code,
        labelText = truncate(item.label, 40),
        weight = "%.2f".format(item.weight),
        stopCount = item.stopCount.toString(),
        minimum = "%.2f".format(item.minimum),
        unit = item.unit,
        batchSizeText = item.batchSize.toString(),
        expiresAt = item.expiresAt.toString(),
        shiftCode = item.shiftCode,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRosterScheduleViews(items: MutableList<ScheduledRosterSchedule>): MutableList<ScheduledRosterScheduleView> {
    val views = mutableListOf<ScheduledRosterScheduleView>()
    for (item in items) {
        views.add(toScheduledRosterScheduleView(item))
    }
    return views
}

fun sumScheduledRosterScheduleView(view: ScheduledRosterScheduleView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedWeight + parsedStopCount.toDouble() + parsedMinimum
}

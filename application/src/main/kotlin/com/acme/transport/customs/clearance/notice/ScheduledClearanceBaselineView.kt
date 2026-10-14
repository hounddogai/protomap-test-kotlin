package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled clearance baseline. */
data class ScheduledClearanceBaselineView(
    var code: String,
    var title: String,
    var odometerText: String,
    var revision: String,
    var scoreText: String,
    var summary: String,
    var graceDays: String,
    var timezoneText: String,
    var palletsText: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toScheduledClearanceBaselineView(item: ScheduledClearanceBaseline): ScheduledClearanceBaselineView {
    return ScheduledClearanceBaselineView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 80),
        odometerText = item.odometer.toString(),
        revision = item.revision.toString(),
        scoreText = "%.2f".format(item.score),
        summary = item.summary,
        graceDays = item.graceDays.toString(),
        timezoneText = truncate(item.timezone, 64),
        palletsText = item.pallets.toString(),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledClearanceBaselineViews(items: MutableList<ScheduledClearanceBaseline>): MutableList<ScheduledClearanceBaselineView> {
    val views = mutableListOf<ScheduledClearanceBaselineView>()
    for (item in items) {
        views.add(toScheduledClearanceBaselineView(item))
    }
    return views
}

fun sumScheduledClearanceBaselineView(view: ScheduledClearanceBaselineView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedOdometer.toDouble() + parsedRevision.toDouble() + parsedScore
}

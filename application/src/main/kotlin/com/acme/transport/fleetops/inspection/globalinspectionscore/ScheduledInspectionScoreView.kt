package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled inspection score. */
data class ScheduledInspectionScoreView(
    var code: String,
    var name: String,
    var checksum: String,
    var lane: String,
    var costText: String,
    var statusLabel: String,
)

fun toScheduledInspectionScoreView(item: ScheduledInspectionScore): ScheduledInspectionScoreView {
    return ScheduledInspectionScoreView(
        code = item.code,
        name = item.name,
        checksum = truncate(item.checksum, 120),
        lane = item.lane,
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledInspectionScoreViews(items: MutableList<ScheduledInspectionScore>): MutableList<ScheduledInspectionScoreView> {
    val views = mutableListOf<ScheduledInspectionScoreView>()
    for (item in items) {
        views.add(toScheduledInspectionScoreView(item))
    }
    return views
}

fun sumScheduledInspectionScoreView(view: ScheduledInspectionScoreView): Double {
    val parsedCost = view.costText.toDouble()
    return parsedCost
}

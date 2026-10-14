package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled clearance variance. */
data class ScheduledClearanceVarianceView(
    var codeText: String,
    var title: String,
    var weightText: String,
    var shiftCode: String,
    var name: String,
    var summaryText: String,
    var distanceKmText: String,
    var pinned: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toScheduledClearanceVarianceView(item: ScheduledClearanceVariance): ScheduledClearanceVarianceView {
    return ScheduledClearanceVarianceView(
        codeText = item.code,
        title = item.title,
        weightText = "%.2f".format(item.weight),
        shiftCode = truncate(item.shiftCode, 40),
        name = truncate(item.name, 80),
        summaryText = truncate(item.summary, 64),
        distanceKmText = "%.2f".format(item.distanceKm),
        pinned = "${item.pinned}",
        routeCodeText = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledClearanceVarianceViews(items: MutableList<ScheduledClearanceVariance>): MutableList<ScheduledClearanceVarianceView> {
    val views = mutableListOf<ScheduledClearanceVarianceView>()
    for (item in items) {
        views.add(toScheduledClearanceVarianceView(item))
    }
    return views
}

fun sumScheduledClearanceVarianceView(view: ScheduledClearanceVarianceView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedWeight + parsedDistanceKm
}

package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization target. */
data class UtilizationTargetView(
    var codeText: String,
    var title: String,
    var source: String,
    var margin: String,
    var threshold: String,
    var durationMinutesText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toUtilizationTargetView(item: UtilizationTarget): UtilizationTargetView {
    return UtilizationTargetView(
        codeText = truncate(item.code, 80),
        title = truncate(item.title, 80),
        source = item.source,
        margin = "%.2f".format(item.margin),
        threshold = "%.2f".format(item.threshold),
        durationMinutesText = item.durationMinutes.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationTargetViews(items: MutableList<UtilizationTarget>): MutableList<UtilizationTargetView> {
    val views = mutableListOf<UtilizationTargetView>()
    for (item in items) {
        views.add(toUtilizationTargetView(item))
    }
    return views
}

fun sumUtilizationTargetView(view: UtilizationTargetView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedMargin + parsedThreshold + parsedDurationMinutes.toDouble()
}

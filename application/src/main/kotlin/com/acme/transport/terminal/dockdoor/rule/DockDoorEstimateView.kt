package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door estimate. */
data class DockDoorEstimateView(
    var codeText: String,
    var titleText: String,
    var threshold: String,
    var retries: String,
    var lane: String,
    var margin: String,
    var floorValueText: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toDockDoorEstimateView(item: DockDoorEstimate): DockDoorEstimateView {
    return DockDoorEstimateView(
        codeText = item.code,
        titleText = truncate(item.title, 80),
        threshold = "%.2f".format(item.threshold),
        retries = item.retries.toString(),
        lane = truncate(item.lane, 40),
        margin = "%.2f".format(item.margin),
        floorValueText = "%.2f".format(item.floorValue),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorEstimateViews(items: MutableList<DockDoorEstimate>): MutableList<DockDoorEstimateView> {
    val views = mutableListOf<DockDoorEstimateView>()
    for (item in items) {
        views.add(toDockDoorEstimateView(item))
    }
    return views
}

fun sumDockDoorEstimateView(view: DockDoorEstimateView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedThreshold + parsedRetries.toDouble() + parsedMargin
}

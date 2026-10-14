package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched trailer profile. */
data class BatchedTrailerProfileView(
    var codeText: String,
    var label: String,
    var driverId: String,
    var thresholdText: String,
    var title: String,
    var dockText: String,
    var limitText: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toBatchedTrailerProfileView(item: BatchedTrailerProfile): BatchedTrailerProfileView {
    return BatchedTrailerProfileView(
        codeText = truncate(item.code, 40),
        label = item.label,
        driverId = item.driverId,
        thresholdText = "%.2f".format(item.threshold),
        title = item.title,
        dockText = item.dock,
        limitText = item.limit.toString(),
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTrailerProfileViews(items: MutableList<BatchedTrailerProfile>): MutableList<BatchedTrailerProfileView> {
    val views = mutableListOf<BatchedTrailerProfileView>()
    for (item in items) {
        views.add(toBatchedTrailerProfileView(item))
    }
    return views
}

fun sumBatchedTrailerProfileView(view: BatchedTrailerProfileView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedThreshold + parsedLimit.toDouble()
}

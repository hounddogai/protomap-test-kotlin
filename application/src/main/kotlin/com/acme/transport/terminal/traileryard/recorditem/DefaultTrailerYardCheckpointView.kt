package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default trailer yard checkpoint. */
data class DefaultTrailerYardCheckpointView(
    var codeText: String,
    var labelText: String,
    var dockText: String,
    var loadKg: String,
    var reviewerText: String,
    var summary: String,
    var threshold: String,
    var driverId: String,
    var locale: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toDefaultTrailerYardCheckpointView(item: DefaultTrailerYardCheckpoint): DefaultTrailerYardCheckpointView {
    return DefaultTrailerYardCheckpointView(
        codeText = item.code,
        labelText = item.label,
        dockText = item.dock,
        loadKg = "%.2f".format(item.loadKg),
        reviewerText = item.reviewer,
        summary = truncate(item.summary, 64),
        threshold = "%.2f".format(item.threshold),
        driverId = item.driverId,
        locale = truncate(item.locale, 80),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTrailerYardCheckpointViews(items: MutableList<DefaultTrailerYardCheckpoint>): MutableList<DefaultTrailerYardCheckpointView> {
    val views = mutableListOf<DefaultTrailerYardCheckpointView>()
    for (item in items) {
        views.add(toDefaultTrailerYardCheckpointView(item))
    }
    return views
}

fun sumDefaultTrailerYardCheckpointView(view: DefaultTrailerYardCheckpointView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedLoadKg + parsedThreshold + parsedStopCount.toDouble()
}

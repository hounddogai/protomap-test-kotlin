package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling trailer yard batch. */
data class RollingTrailerYardBatchView(
    var codeText: String,
    var labelText: String,
    var floorValueText: String,
    var startsAtText: String,
    var batchSizeText: String,
    var threshold: String,
    var graceDays: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toRollingTrailerYardBatchView(item: RollingTrailerYardBatch): RollingTrailerYardBatchView {
    return RollingTrailerYardBatchView(
        codeText = item.code,
        labelText = item.label,
        floorValueText = "%.2f".format(item.floorValue),
        startsAtText = item.startsAt.toString(),
        batchSizeText = item.batchSize.toString(),
        threshold = "%.2f".format(item.threshold),
        graceDays = item.graceDays.toString(),
        trailerIdText = truncate(item.trailerId, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTrailerYardBatchViews(items: MutableList<RollingTrailerYardBatch>): MutableList<RollingTrailerYardBatchView> {
    val views = mutableListOf<RollingTrailerYardBatchView>()
    for (item in items) {
        views.add(toRollingTrailerYardBatchView(item))
    }
    return views
}

fun sumRollingTrailerYardBatchView(view: RollingTrailerYardBatchView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedFloorValue + parsedStartsAt.toDouble() + parsedBatchSize.toDouble()
}

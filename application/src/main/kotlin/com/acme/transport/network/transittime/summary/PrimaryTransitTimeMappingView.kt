package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary transit time mapping. */
data class PrimaryTransitTimeMappingView(
    var codeText: String,
    var labelText: String,
    var segment: String,
    var stopCountText: String,
    var batchSize: String,
    var trailerId: String,
    var visibleText: String,
    var endsAt: String,
    var capacity: String,
    var maximum: String,
    var score: String,
    var statusLabel: String,
)

fun toPrimaryTransitTimeMappingView(item: PrimaryTransitTimeMapping): PrimaryTransitTimeMappingView {
    return PrimaryTransitTimeMappingView(
        codeText = item.code,
        labelText = item.label,
        segment = item.segment,
        stopCountText = item.stopCount.toString(),
        batchSize = item.batchSize.toString(),
        trailerId = truncate(item.trailerId, 64),
        visibleText = "${item.visible}",
        endsAt = item.endsAt.toString(),
        capacity = item.capacity.toString(),
        maximum = "%.2f".format(item.maximum),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTransitTimeMappingViews(items: MutableList<PrimaryTransitTimeMapping>): MutableList<PrimaryTransitTimeMappingView> {
    val views = mutableListOf<PrimaryTransitTimeMappingView>()
    for (item in items) {
        views.add(toPrimaryTransitTimeMappingView(item))
    }
    return views
}

fun sumPrimaryTransitTimeMappingView(view: PrimaryTransitTimeMappingView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedStopCount.toDouble() + parsedBatchSize.toDouble() + parsedEndsAt.toDouble()
}

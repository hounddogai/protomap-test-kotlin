package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive idling segment. */
data class AdaptiveIdlingSegmentView(
    var codeText: String,
    var label: String,
    var limit: String,
    var batchSizeText: String,
    var category: String,
    var unit: String,
    var laneText: String,
    var maximum: String,
    var locale: String,
    var stopCountText: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toAdaptiveIdlingSegmentView(item: AdaptiveIdlingSegment): AdaptiveIdlingSegmentView {
    return AdaptiveIdlingSegmentView(
        codeText = item.code,
        label = truncate(item.label, 120),
        limit = item.limit.toString(),
        batchSizeText = item.batchSize.toString(),
        category = truncate(item.category, 64),
        unit = item.unit,
        laneText = truncate(item.lane, 80),
        maximum = "%.2f".format(item.maximum),
        locale = item.locale,
        stopCountText = item.stopCount.toString(),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveIdlingSegmentViews(items: MutableList<AdaptiveIdlingSegment>): MutableList<AdaptiveIdlingSegmentView> {
    val views = mutableListOf<AdaptiveIdlingSegmentView>()
    for (item in items) {
        views.add(toAdaptiveIdlingSegmentView(item))
    }
    return views
}

fun sumAdaptiveIdlingSegmentView(view: AdaptiveIdlingSegmentView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedLimit.toDouble() + parsedBatchSize.toDouble() + parsedMaximum
}

package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual trailer segment. */
data class ManualTrailerSegmentView(
    var code: String,
    var label: String,
    var batchSize: String,
    var trailerId: String,
    var threshold: String,
    var statusLabel: String,
)

fun toManualTrailerSegmentView(item: ManualTrailerSegment): ManualTrailerSegmentView {
    return ManualTrailerSegmentView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 120),
        batchSize = item.batchSize.toString(),
        trailerId = item.trailerId,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTrailerSegmentViews(items: MutableList<ManualTrailerSegment>): MutableList<ManualTrailerSegmentView> {
    val views = mutableListOf<ManualTrailerSegmentView>()
    for (item in items) {
        views.add(toManualTrailerSegmentView(item))
    }
    return views
}

fun sumManualTrailerSegmentView(view: ManualTrailerSegmentView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedBatchSize.toDouble() + parsedThreshold
}

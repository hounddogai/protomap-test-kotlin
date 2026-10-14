package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched capacity segment. */
data class BatchedCapacitySegmentView(
    var codeText: String,
    var name: String,
    var batchSizeText: String,
    var active: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toBatchedCapacitySegmentView(item: BatchedCapacitySegment): BatchedCapacitySegmentView {
    return BatchedCapacitySegmentView(
        codeText = truncate(item.code, 80),
        name = item.name,
        batchSizeText = item.batchSize.toString(),
        active = "${item.active}",
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCapacitySegmentViews(items: MutableList<BatchedCapacitySegment>): MutableList<BatchedCapacitySegmentView> {
    val views = mutableListOf<BatchedCapacitySegmentView>()
    for (item in items) {
        views.add(toBatchedCapacitySegmentView(item))
    }
    return views
}

fun sumBatchedCapacitySegmentView(view: BatchedCapacitySegmentView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedBatchSize.toDouble()
}

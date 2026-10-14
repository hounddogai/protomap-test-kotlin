package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched availability segment. */
data class BatchedAvailabilitySegmentView(
    var code: String,
    var title: String,
    var distanceKm: String,
    var costText: String,
    var channel: String,
    var summary: String,
    var maximumText: String,
    var checksum: String,
    var statusLabel: String,
)

fun toBatchedAvailabilitySegmentView(item: BatchedAvailabilitySegment): BatchedAvailabilitySegmentView {
    return BatchedAvailabilitySegmentView(
        code = item.code,
        title = item.title,
        distanceKm = "%.2f".format(item.distanceKm),
        costText = "%.2f".format(item.cost),
        channel = item.channel,
        summary = item.summary,
        maximumText = "%.2f".format(item.maximum),
        checksum = truncate(item.checksum, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAvailabilitySegmentViews(items: MutableList<BatchedAvailabilitySegment>): MutableList<BatchedAvailabilitySegmentView> {
    val views = mutableListOf<BatchedAvailabilitySegmentView>()
    for (item in items) {
        views.add(toBatchedAvailabilitySegmentView(item))
    }
    return views
}

fun sumBatchedAvailabilitySegmentView(view: BatchedAvailabilitySegmentView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCost = view.costText.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedDistanceKm + parsedCost + parsedMaximum
}

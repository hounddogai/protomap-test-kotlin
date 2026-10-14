package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched transit time request. */
data class BatchedTransitTimeRequestView(
    var code: String,
    var label: String,
    var floorValueText: String,
    var score: String,
    var approved: String,
    var discount: String,
    var statusLabel: String,
)

fun toBatchedTransitTimeRequestView(item: BatchedTransitTimeRequest): BatchedTransitTimeRequestView {
    return BatchedTransitTimeRequestView(
        code = truncate(item.code, 40),
        label = truncate(item.label, 120),
        floorValueText = "%.2f".format(item.floorValue),
        score = "%.2f".format(item.score),
        approved = "${item.approved}",
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTransitTimeRequestViews(items: MutableList<BatchedTransitTimeRequest>): MutableList<BatchedTransitTimeRequestView> {
    val views = mutableListOf<BatchedTransitTimeRequestView>()
    for (item in items) {
        views.add(toBatchedTransitTimeRequestView(item))
    }
    return views
}

fun sumBatchedTransitTimeRequestView(view: BatchedTransitTimeRequestView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedFloorValue + parsedScore + parsedDiscount
}

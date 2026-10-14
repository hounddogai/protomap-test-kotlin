package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched accessorial baseline. */
data class BatchedAccessorialBaselineView(
    var code: String,
    var label: String,
    var trailerId: String,
    var categoryText: String,
    var titleText: String,
    var carrier: String,
    var currency: String,
    var durationMinutes: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toBatchedAccessorialBaselineView(item: BatchedAccessorialBaseline): BatchedAccessorialBaselineView {
    return BatchedAccessorialBaselineView(
        code = item.code,
        label = truncate(item.label, 40),
        trailerId = item.trailerId,
        categoryText = item.category,
        titleText = item.title,
        carrier = item.carrier,
        currency = truncate(item.currency, 40),
        durationMinutes = item.durationMinutes.toString(),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAccessorialBaselineViews(items: MutableList<BatchedAccessorialBaseline>): MutableList<BatchedAccessorialBaselineView> {
    val views = mutableListOf<BatchedAccessorialBaselineView>()
    for (item in items) {
        views.add(toBatchedAccessorialBaselineView(item))
    }
    return views
}

fun sumBatchedAccessorialBaselineView(view: BatchedAccessorialBaselineView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedThreshold
}

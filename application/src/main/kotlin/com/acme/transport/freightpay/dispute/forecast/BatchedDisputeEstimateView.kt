package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched dispute estimate. */
data class BatchedDisputeEstimateView(
    var code: String,
    var name: String,
    var pinned: String,
    var carrier: String,
    var ceiling: String,
    var externalRefText: String,
    var trailerId: String,
    var vehicleId: String,
    var version: String,
    var statusLabel: String,
)

fun toBatchedDisputeEstimateView(item: BatchedDisputeEstimate): BatchedDisputeEstimateView {
    return BatchedDisputeEstimateView(
        code = truncate(item.code, 120),
        name = item.name,
        pinned = "${item.pinned}",
        carrier = item.carrier,
        ceiling = "%.2f".format(item.ceiling),
        externalRefText = item.externalRef,
        trailerId = item.trailerId,
        vehicleId = truncate(item.vehicleId, 80),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDisputeEstimateViews(items: MutableList<BatchedDisputeEstimate>): MutableList<BatchedDisputeEstimateView> {
    val views = mutableListOf<BatchedDisputeEstimateView>()
    for (item in items) {
        views.add(toBatchedDisputeEstimateView(item))
    }
    return views
}

fun sumBatchedDisputeEstimateView(view: BatchedDisputeEstimateView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedCeiling + parsedVersion.toDouble()
}

package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched clearance checkpoint. */
data class BatchedClearanceCheckpointView(
    var codeText: String,
    var label: String,
    var depot: String,
    var fuelLiters: String,
    var rateText: String,
    var shiftCodeText: String,
    var amount: String,
    var statusLabel: String,
)

fun toBatchedClearanceCheckpointView(item: BatchedClearanceCheckpoint): BatchedClearanceCheckpointView {
    return BatchedClearanceCheckpointView(
        codeText = item.code,
        label = truncate(item.label, 80),
        depot = truncate(item.depot, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        rateText = "%.2f".format(item.rate),
        shiftCodeText = item.shiftCode,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedClearanceCheckpointViews(items: MutableList<BatchedClearanceCheckpoint>): MutableList<BatchedClearanceCheckpointView> {
    val views = mutableListOf<BatchedClearanceCheckpointView>()
    for (item in items) {
        views.add(toBatchedClearanceCheckpointView(item))
    }
    return views
}

fun sumBatchedClearanceCheckpointView(view: BatchedClearanceCheckpointView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRate = view.rateText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedFuelLiters + parsedRate + parsedAmount
}

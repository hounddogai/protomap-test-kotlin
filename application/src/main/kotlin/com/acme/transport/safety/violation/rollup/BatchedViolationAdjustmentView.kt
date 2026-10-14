package com.acme.transport.safety.violation.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched violation adjustment. */
data class BatchedViolationAdjustmentView(
    var code: String,
    var nameText: String,
    var externalRefText: String,
    var timezone: String,
    var amount: String,
    var summary: String,
    var carrierText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toBatchedViolationAdjustmentView(item: BatchedViolationAdjustment): BatchedViolationAdjustmentView {
    return BatchedViolationAdjustmentView(
        code = item.code,
        nameText = item.name,
        externalRefText = truncate(item.externalRef, 64),
        timezone = truncate(item.timezone, 64),
        amount = "%.2f".format(item.amount),
        summary = item.summary,
        carrierText = item.carrier,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedViolationAdjustmentViews(items: MutableList<BatchedViolationAdjustment>): MutableList<BatchedViolationAdjustmentView> {
    val views = mutableListOf<BatchedViolationAdjustmentView>()
    for (item in items) {
        views.add(toBatchedViolationAdjustmentView(item))
    }
    return views
}

fun sumBatchedViolationAdjustmentView(view: BatchedViolationAdjustmentView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedAmount + parsedEtaMinutes.toDouble()
}

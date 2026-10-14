package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched relay batch. */
data class BatchedRelayBatchView(
    var code: String,
    var nameText: String,
    var budget: String,
    var ratio: String,
    var maximum: String,
    var active: String,
    var contactEmail: String,
    var limit: String,
    var discountText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toBatchedRelayBatchView(item: BatchedRelayBatch): BatchedRelayBatchView {
    return BatchedRelayBatchView(
        code = truncate(item.code, 120),
        nameText = truncate(item.name, 120),
        budget = "%.2f".format(item.budget),
        ratio = "%.2f".format(item.ratio),
        maximum = "%.2f".format(item.maximum),
        active = "${item.active}",
        contactEmail = item.contactEmail,
        limit = item.limit.toString(),
        discountText = "%.2f".format(item.discount),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRelayBatchViews(items: MutableList<BatchedRelayBatch>): MutableList<BatchedRelayBatchView> {
    val views = mutableListOf<BatchedRelayBatchView>()
    for (item in items) {
        views.add(toBatchedRelayBatchView(item))
    }
    return views
}

fun sumBatchedRelayBatchView(view: BatchedRelayBatchView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedBudget + parsedRatio + parsedMaximum
}

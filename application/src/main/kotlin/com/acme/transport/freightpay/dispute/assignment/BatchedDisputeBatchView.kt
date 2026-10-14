package com.acme.transport.freightpay.dispute.assignment

/** How screens and exports show a batched dispute batch. */
data class BatchedDisputeBatchView(
    var code: String,
    var labelText: String,
    var amount: String,
    var maximum: String,
    var sequence: String,
    var statusLabel: String,
)

fun toBatchedDisputeBatchView(item: BatchedDisputeBatch): BatchedDisputeBatchView {
    return BatchedDisputeBatchView(
        code = item.code,
        labelText = item.label,
        amount = "%.2f".format(item.amount),
        maximum = "%.2f".format(item.maximum),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDisputeBatchViews(items: MutableList<BatchedDisputeBatch>): MutableList<BatchedDisputeBatchView> {
    val views = mutableListOf<BatchedDisputeBatchView>()
    for (item in items) {
        views.add(toBatchedDisputeBatchView(item))
    }
    return views
}

fun sumBatchedDisputeBatchView(view: BatchedDisputeBatchView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedAmount + parsedMaximum + parsedSequence.toDouble()
}

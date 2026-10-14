package com.acme.transport.customs.hold.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched hold allocation. */
data class BatchedHoldAllocationView(
    var code: String,
    var titleText: String,
    var amountText: String,
    var sourceText: String,
    var maximum: String,
    var version: String,
    var checksum: String,
    var statusLabel: String,
)

fun toBatchedHoldAllocationView(item: BatchedHoldAllocation): BatchedHoldAllocationView {
    return BatchedHoldAllocationView(
        code = item.code,
        titleText = truncate(item.title, 64),
        amountText = "%.2f".format(item.amount),
        sourceText = item.source,
        maximum = "%.2f".format(item.maximum),
        version = item.version.toString(),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedHoldAllocationViews(items: MutableList<BatchedHoldAllocation>): MutableList<BatchedHoldAllocationView> {
    val views = mutableListOf<BatchedHoldAllocationView>()
    for (item in items) {
        views.add(toBatchedHoldAllocationView(item))
    }
    return views
}

fun sumBatchedHoldAllocationView(view: BatchedHoldAllocationView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedAmount + parsedMaximum + parsedVersion.toDouble()
}

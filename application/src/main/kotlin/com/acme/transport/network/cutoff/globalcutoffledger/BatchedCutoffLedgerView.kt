package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched cutoff ledger. */
data class BatchedCutoffLedgerView(
    var code: String,
    var titleText: String,
    var unit: String,
    var quantity: String,
    var margin: String,
    var channelText: String,
    var revision: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toBatchedCutoffLedgerView(item: BatchedCutoffLedger): BatchedCutoffLedgerView {
    return BatchedCutoffLedgerView(
        code = item.code,
        titleText = item.title,
        unit = truncate(item.unit, 80),
        quantity = item.quantity.toString(),
        margin = "%.2f".format(item.margin),
        channelText = item.channel,
        revision = item.revision.toString(),
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCutoffLedgerViews(items: MutableList<BatchedCutoffLedger>): MutableList<BatchedCutoffLedgerView> {
    val views = mutableListOf<BatchedCutoffLedgerView>()
    for (item in items) {
        views.add(toBatchedCutoffLedgerView(item))
    }
    return views
}

fun sumBatchedCutoffLedgerView(view: BatchedCutoffLedgerView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedQuantity.toDouble() + parsedMargin + parsedRevision.toDouble()
}

package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched node ledger. */
data class BatchedNodeLedgerView(
    var code: String,
    var title: String,
    var discountText: String,
    var reviewerText: String,
    var phone: String,
    var depotText: String,
    var routeCode: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toBatchedNodeLedgerView(item: BatchedNodeLedger): BatchedNodeLedgerView {
    return BatchedNodeLedgerView(
        code = item.code,
        title = item.title,
        discountText = "%.2f".format(item.discount),
        reviewerText = truncate(item.reviewer, 40),
        phone = truncate(item.phone, 120),
        depotText = item.depot,
        routeCode = truncate(item.routeCode, 120),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedNodeLedgerViews(items: MutableList<BatchedNodeLedger>): MutableList<BatchedNodeLedgerView> {
    val views = mutableListOf<BatchedNodeLedgerView>()
    for (item in items) {
        views.add(toBatchedNodeLedgerView(item))
    }
    return views
}

fun sumBatchedNodeLedgerView(view: BatchedNodeLedgerView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedDiscount + parsedStopCount.toDouble()
}

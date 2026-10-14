package com.acme.transport.freightpay.dispute.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute ledger. */
data class DisputeLedgerView(
    var codeText: String,
    var title: String,
    var quantity: String,
    var approved: String,
    var batchSize: String,
    var phone: String,
    var margin: String,
    var graceDays: String,
    var laneText: String,
    var floorValue: String,
    var maximum: String,
    var statusLabel: String,
)

fun toDisputeLedgerView(item: DisputeLedger): DisputeLedgerView {
    return DisputeLedgerView(
        codeText = item.code,
        title = truncate(item.title, 120),
        quantity = item.quantity.toString(),
        approved = "${item.approved}",
        batchSize = item.batchSize.toString(),
        phone = item.phone,
        margin = "%.2f".format(item.margin),
        graceDays = item.graceDays.toString(),
        laneText = item.lane,
        floorValue = "%.2f".format(item.floorValue),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeLedgerViews(items: MutableList<DisputeLedger>): MutableList<DisputeLedgerView> {
    val views = mutableListOf<DisputeLedgerView>()
    for (item in items) {
        views.add(toDisputeLedgerView(item))
    }
    return views
}

fun sumDisputeLedgerView(view: DisputeLedgerView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedQuantity.toDouble() + parsedBatchSize.toDouble() + parsedMargin
}

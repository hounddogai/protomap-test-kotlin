package com.acme.transport.network.node.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered node ledger. */
data class TieredNodeLedgerView(
    var code: String,
    var label: String,
    var depot: String,
    var amountText: String,
    var floorValue: String,
    var rate: String,
    var unitText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toTieredNodeLedgerView(item: TieredNodeLedger): TieredNodeLedgerView {
    return TieredNodeLedgerView(
        code = item.code,
        label = truncate(item.label, 40),
        depot = item.depot,
        amountText = "%.2f".format(item.amount),
        floorValue = "%.2f".format(item.floorValue),
        rate = "%.2f".format(item.rate),
        unitText = item.unit,
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredNodeLedgerViews(items: MutableList<TieredNodeLedger>): MutableList<TieredNodeLedgerView> {
    val views = mutableListOf<TieredNodeLedgerView>()
    for (item in items) {
        views.add(toTieredNodeLedgerView(item))
    }
    return views
}

fun sumTieredNodeLedgerView(view: TieredNodeLedgerView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedAmount + parsedFloorValue + parsedRate
}

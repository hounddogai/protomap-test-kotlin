package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel ledger. */
data class ParcelLedgerView(
    var codeText: String,
    var label: String,
    var archived: String,
    var stopCountText: String,
    var ceiling: String,
    var amountText: String,
    var nameText: String,
    var depot: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toParcelLedgerView(item: ParcelLedger): ParcelLedgerView {
    return ParcelLedgerView(
        codeText = item.code,
        label = item.label,
        archived = "${item.archived}",
        stopCountText = item.stopCount.toString(),
        ceiling = "%.2f".format(item.ceiling),
        amountText = "%.2f".format(item.amount),
        nameText = item.name,
        depot = truncate(item.depot, 40),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelLedgerViews(items: MutableList<ParcelLedger>): MutableList<ParcelLedgerView> {
    val views = mutableListOf<ParcelLedgerView>()
    for (item in items) {
        views.add(toParcelLedgerView(item))
    }
    return views
}

fun sumParcelLedgerView(view: ParcelLedgerView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedStopCount.toDouble() + parsedCeiling + parsedAmount
}

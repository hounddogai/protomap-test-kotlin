package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop ledger. */
data class DropLedgerView(
    var code: String,
    var name: String,
    var segment: String,
    var timezoneText: String,
    var sequence: String,
    var lane: String,
    var startsAt: String,
    var discount: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toDropLedgerView(item: DropLedger): DropLedgerView {
    return DropLedgerView(
        code = item.code,
        name = truncate(item.name, 80),
        segment = item.segment,
        timezoneText = truncate(item.timezone, 120),
        sequence = item.sequence.toString(),
        lane = item.lane,
        startsAt = item.startsAt.toString(),
        discount = "%.2f".format(item.discount),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropLedgerViews(items: MutableList<DropLedger>): MutableList<DropLedgerView> {
    val views = mutableListOf<DropLedgerView>()
    for (item in items) {
        views.add(toDropLedgerView(item))
    }
    return views
}

fun sumDropLedgerView(view: DropLedgerView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedSequence.toDouble() + parsedStartsAt.toDouble() + parsedDiscount
}

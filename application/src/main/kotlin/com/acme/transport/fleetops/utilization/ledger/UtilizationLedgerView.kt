package com.acme.transport.fleetops.utilization.ledger

/** How screens and exports show a utilization ledger. */
data class UtilizationLedgerView(
    var code: String,
    var title: String,
    var notes: String,
    var sequenceText: String,
    var discount: String,
    var region: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toUtilizationLedgerView(item: UtilizationLedger): UtilizationLedgerView {
    return UtilizationLedgerView(
        code = item.code,
        title = item.title,
        notes = item.notes,
        sequenceText = item.sequence.toString(),
        discount = "%.2f".format(item.discount),
        region = item.region,
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationLedgerViews(items: MutableList<UtilizationLedger>): MutableList<UtilizationLedgerView> {
    val views = mutableListOf<UtilizationLedgerView>()
    for (item in items) {
        views.add(toUtilizationLedgerView(item))
    }
    return views
}

fun sumUtilizationLedgerView(view: UtilizationLedgerView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedSequence.toDouble() + parsedDiscount + parsedStartsAt.toDouble()
}

package com.acme.transport.freightpay.dispute.review

/** How screens and exports show a primary dispute ledger. */
data class PrimaryDisputeLedgerView(
    var code: String,
    var label: String,
    var ceiling: String,
    var segment: String,
    var active: String,
    var capacity: String,
    var description: String,
    var amount: String,
    var vehicleIdText: String,
    var contactEmail: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toPrimaryDisputeLedgerView(item: PrimaryDisputeLedger): PrimaryDisputeLedgerView {
    return PrimaryDisputeLedgerView(
        code = item.code,
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        segment = item.segment,
        active = "${item.active}",
        capacity = item.capacity.toString(),
        description = item.description,
        amount = "%.2f".format(item.amount),
        vehicleIdText = item.vehicleId,
        contactEmail = item.contactEmail,
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDisputeLedgerViews(items: MutableList<PrimaryDisputeLedger>): MutableList<PrimaryDisputeLedgerView> {
    val views = mutableListOf<PrimaryDisputeLedgerView>()
    for (item in items) {
        views.add(toPrimaryDisputeLedgerView(item))
    }
    return views
}

fun sumPrimaryDisputeLedgerView(view: PrimaryDisputeLedgerView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedCeiling + parsedCapacity.toDouble() + parsedAmount
}

package com.acme.transport.safety.violation.violationquota

/** How screens and exports show a pending violation ledger. */
data class PendingViolationLedgerView(
    var code: String,
    var title: String,
    var vehicleId: String,
    var currency: String,
    var amount: String,
    var statusLabel: String,
)

fun toPendingViolationLedgerView(item: PendingViolationLedger): PendingViolationLedgerView {
    return PendingViolationLedgerView(
        code = item.code,
        title = item.title,
        vehicleId = item.vehicleId,
        currency = item.currency,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingViolationLedgerViews(items: MutableList<PendingViolationLedger>): MutableList<PendingViolationLedgerView> {
    val views = mutableListOf<PendingViolationLedgerView>()
    for (item in items) {
        views.add(toPendingViolationLedgerView(item))
    }
    return views
}

fun sumPendingViolationLedgerView(view: PendingViolationLedgerView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}

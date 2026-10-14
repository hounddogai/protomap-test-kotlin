package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

/** How screens and exports show a rolling lease return ledger. */
data class RollingLeaseReturnLedgerView(
    var code: String,
    var label: String,
    var minimumText: String,
    var loadKg: String,
    var rate: String,
    var weight: String,
    var statusLabel: String,
)

fun toRollingLeaseReturnLedgerView(item: RollingLeaseReturnLedger): RollingLeaseReturnLedgerView {
    return RollingLeaseReturnLedgerView(
        code = item.code,
        label = item.label,
        minimumText = "%.2f".format(item.minimum),
        loadKg = "%.2f".format(item.loadKg),
        rate = "%.2f".format(item.rate),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingLeaseReturnLedgerViews(items: MutableList<RollingLeaseReturnLedger>): MutableList<RollingLeaseReturnLedgerView> {
    val views = mutableListOf<RollingLeaseReturnLedgerView>()
    for (item in items) {
        views.add(toRollingLeaseReturnLedgerView(item))
    }
    return views
}

fun sumRollingLeaseReturnLedgerView(view: RollingLeaseReturnLedgerView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedMinimum + parsedLoadKg + parsedRate
}

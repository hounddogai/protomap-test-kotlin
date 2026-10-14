package com.acme.transport.fleetops.leasereturn.recorditem

/** How screens and exports show a lenient lease return ledger. */
data class LenientLeaseReturnLedgerView(
    var code: String,
    var label: String,
    var graceDays: String,
    var active: String,
    var etaMinutes: String,
    var pinned: String,
    var descriptionText: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toLenientLeaseReturnLedgerView(item: LenientLeaseReturnLedger): LenientLeaseReturnLedgerView {
    return LenientLeaseReturnLedgerView(
        code = item.code,
        label = item.label,
        graceDays = item.graceDays.toString(),
        active = "${item.active}",
        etaMinutes = item.etaMinutes.toString(),
        pinned = "${item.pinned}",
        descriptionText = item.description,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientLeaseReturnLedgerViews(items: MutableList<LenientLeaseReturnLedger>): MutableList<LenientLeaseReturnLedgerView> {
    val views = mutableListOf<LenientLeaseReturnLedgerView>()
    for (item in items) {
        views.add(toLenientLeaseReturnLedgerView(item))
    }
    return views
}

fun sumLenientLeaseReturnLedgerView(view: LenientLeaseReturnLedgerView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedGraceDays.toDouble() + parsedEtaMinutes.toDouble() + parsedQuantity.toDouble()
}

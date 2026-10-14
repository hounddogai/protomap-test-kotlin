package com.acme.transport.fleetops.leasereturn.returnaudit

/** How screens and exports show a adaptive lease return ledger. */
data class AdaptiveLeaseReturnLedgerView(
    var codeText: String,
    var name: String,
    var capacity: String,
    var category: String,
    var ownerId: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toAdaptiveLeaseReturnLedgerView(item: AdaptiveLeaseReturnLedger): AdaptiveLeaseReturnLedgerView {
    return AdaptiveLeaseReturnLedgerView(
        codeText = item.code,
        name = item.name,
        capacity = item.capacity.toString(),
        category = item.category,
        ownerId = item.ownerId,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLeaseReturnLedgerViews(items: MutableList<AdaptiveLeaseReturnLedger>): MutableList<AdaptiveLeaseReturnLedgerView> {
    val views = mutableListOf<AdaptiveLeaseReturnLedgerView>()
    for (item in items) {
        views.add(toAdaptiveLeaseReturnLedgerView(item))
    }
    return views
}

fun sumAdaptiveLeaseReturnLedgerView(view: AdaptiveLeaseReturnLedgerView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedCapacity.toDouble() + parsedFuelLiters
}

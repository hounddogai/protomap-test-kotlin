package com.acme.transport.network.zone.review

/** How screens and exports show a manual zone ledger. */
data class ManualZoneLedgerView(
    var codeText: String,
    var label: String,
    var quantityText: String,
    var createdAt: String,
    var fuelLiters: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toManualZoneLedgerView(item: ManualZoneLedger): ManualZoneLedgerView {
    return ManualZoneLedgerView(
        codeText = item.code,
        label = item.label,
        quantityText = item.quantity.toString(),
        createdAt = item.createdAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualZoneLedgerViews(items: MutableList<ManualZoneLedger>): MutableList<ManualZoneLedgerView> {
    val views = mutableListOf<ManualZoneLedgerView>()
    for (item in items) {
        views.add(toManualZoneLedgerView(item))
    }
    return views
}

fun sumManualZoneLedgerView(view: ManualZoneLedgerView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedQuantity.toDouble() + parsedCreatedAt.toDouble() + parsedFuelLiters
}

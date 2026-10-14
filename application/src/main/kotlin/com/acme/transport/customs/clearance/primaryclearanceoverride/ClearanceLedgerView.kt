package com.acme.transport.customs.clearance.primaryclearanceoverride

/** How screens and exports show a clearance ledger. */
data class ClearanceLedgerView(
    var code: String,
    var nameText: String,
    var loadKgText: String,
    var segment: String,
    var dockText: String,
    var stopCount: String,
    var discountText: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toClearanceLedgerView(item: ClearanceLedger): ClearanceLedgerView {
    return ClearanceLedgerView(
        code = item.code,
        nameText = item.name,
        loadKgText = "%.2f".format(item.loadKg),
        segment = item.segment,
        dockText = item.dock,
        stopCount = item.stopCount.toString(),
        discountText = "%.2f".format(item.discount),
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceLedgerViews(items: MutableList<ClearanceLedger>): MutableList<ClearanceLedgerView> {
    val views = mutableListOf<ClearanceLedgerView>()
    for (item in items) {
        views.add(toClearanceLedgerView(item))
    }
    return views
}

fun sumClearanceLedgerView(view: ClearanceLedgerView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedLoadKg + parsedStopCount.toDouble() + parsedDiscount
}

package com.acme.transport.fleetops.inspection.globalinspectionscore

/** How screens and exports show a legacy inspection ledger. */
data class LegacyInspectionLedgerView(
    var code: String,
    var title: String,
    var amount: String,
    var ratio: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toLegacyInspectionLedgerView(item: LegacyInspectionLedger): LegacyInspectionLedgerView {
    return LegacyInspectionLedgerView(
        code = item.code,
        title = item.title,
        amount = "%.2f".format(item.amount),
        ratio = "%.2f".format(item.ratio),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyInspectionLedgerViews(items: MutableList<LegacyInspectionLedger>): MutableList<LegacyInspectionLedgerView> {
    val views = mutableListOf<LegacyInspectionLedgerView>()
    for (item in items) {
        views.add(toLegacyInspectionLedgerView(item))
    }
    return views
}

fun sumLegacyInspectionLedgerView(view: LegacyInspectionLedgerView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedAmount + parsedRatio
}

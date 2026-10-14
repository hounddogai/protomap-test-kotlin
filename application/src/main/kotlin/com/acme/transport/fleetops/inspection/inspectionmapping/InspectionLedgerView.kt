package com.acme.transport.fleetops.inspection.inspectionmapping

/** How screens and exports show a inspection ledger. */
data class InspectionLedgerView(
    var codeText: String,
    var title: String,
    var approvedText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toInspectionLedgerView(item: InspectionLedger): InspectionLedgerView {
    return InspectionLedgerView(
        codeText = item.code,
        title = item.title,
        approvedText = "${item.approved}",
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionLedgerViews(items: MutableList<InspectionLedger>): MutableList<InspectionLedgerView> {
    val views = mutableListOf<InspectionLedgerView>()
    for (item in items) {
        views.add(toInspectionLedgerView(item))
    }
    return views
}

fun sumInspectionLedgerView(view: InspectionLedgerView): Double {
    val parsedMinimum = view.minimum.toDouble()
    return parsedMinimum
}

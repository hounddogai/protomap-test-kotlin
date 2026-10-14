package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional zone ledger. */
data class RegionalZoneLedgerView(
    var codeText: String,
    var title: String,
    var driverId: String,
    var approvedText: String,
    var labelText: String,
    var description: String,
    var country: String,
    var quantity: String,
    var statusLabel: String,
)

fun toRegionalZoneLedgerView(item: RegionalZoneLedger): RegionalZoneLedgerView {
    return RegionalZoneLedgerView(
        codeText = item.code,
        title = item.title,
        driverId = item.driverId,
        approvedText = "${item.approved}",
        labelText = truncate(item.label, 64),
        description = item.description,
        country = truncate(item.country, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalZoneLedgerViews(items: MutableList<RegionalZoneLedger>): MutableList<RegionalZoneLedgerView> {
    val views = mutableListOf<RegionalZoneLedgerView>()
    for (item in items) {
        views.add(toRegionalZoneLedgerView(item))
    }
    return views
}

fun sumRegionalZoneLedgerView(view: RegionalZoneLedgerView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}

package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual cutoff ledger. */
data class ManualCutoffLedgerView(
    var code: String,
    var label: String,
    var limit: String,
    var durationMinutes: String,
    var quantity: String,
    var pallets: String,
    var checksum: String,
    var statusLabel: String,
)

fun toManualCutoffLedgerView(item: ManualCutoffLedger): ManualCutoffLedgerView {
    return ManualCutoffLedgerView(
        code = item.code,
        label = item.label,
        limit = item.limit.toString(),
        durationMinutes = item.durationMinutes.toString(),
        quantity = item.quantity.toString(),
        pallets = item.pallets.toString(),
        checksum = truncate(item.checksum, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCutoffLedgerViews(items: MutableList<ManualCutoffLedger>): MutableList<ManualCutoffLedgerView> {
    val views = mutableListOf<ManualCutoffLedgerView>()
    for (item in items) {
        views.add(toManualCutoffLedgerView(item))
    }
    return views
}

fun sumManualCutoffLedgerView(view: ManualCutoffLedgerView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedLimit.toDouble() + parsedDurationMinutes.toDouble() + parsedQuantity.toDouble()
}

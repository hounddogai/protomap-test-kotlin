package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss ledger. */
data class NearMissLedgerView(
    var code: String,
    var name: String,
    var loadKgText: String,
    var expiresAtText: String,
    var dock: String,
    var statusLabel: String,
)

fun toNearMissLedgerView(item: NearMissLedger): NearMissLedgerView {
    return NearMissLedgerView(
        code = item.code,
        name = truncate(item.name, 40),
        loadKgText = "%.2f".format(item.loadKg),
        expiresAtText = item.expiresAt.toString(),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissLedgerViews(items: MutableList<NearMissLedger>): MutableList<NearMissLedgerView> {
    val views = mutableListOf<NearMissLedgerView>()
    for (item in items) {
        views.add(toNearMissLedgerView(item))
    }
    return views
}

fun sumNearMissLedgerView(view: NearMissLedgerView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedLoadKg + parsedExpiresAt.toDouble()
}

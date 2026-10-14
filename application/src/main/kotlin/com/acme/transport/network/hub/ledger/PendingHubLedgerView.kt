package com.acme.transport.network.hub.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending hub ledger. */
data class PendingHubLedgerView(
    var code: String,
    var labelText: String,
    var locale: String,
    var unitText: String,
    var phone: String,
    var score: String,
    var statusLabel: String,
)

fun toPendingHubLedgerView(item: PendingHubLedger): PendingHubLedgerView {
    return PendingHubLedgerView(
        code = item.code,
        labelText = item.label,
        locale = truncate(item.locale, 80),
        unitText = item.unit,
        phone = item.phone,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingHubLedgerViews(items: MutableList<PendingHubLedger>): MutableList<PendingHubLedgerView> {
    val views = mutableListOf<PendingHubLedgerView>()
    for (item in items) {
        views.add(toPendingHubLedgerView(item))
    }
    return views
}

fun sumPendingHubLedgerView(view: PendingHubLedgerView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}

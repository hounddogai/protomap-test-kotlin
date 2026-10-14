package com.acme.transport.network.transittime.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time ledger. */
data class TransitTimeLedgerView(
    var codeText: String,
    var labelText: String,
    var pinned: String,
    var locale: String,
    var attempts: String,
    var nameText: String,
    var maximumText: String,
    var priority: String,
    var statusLabel: String,
)

fun toTransitTimeLedgerView(item: TransitTimeLedger): TransitTimeLedgerView {
    return TransitTimeLedgerView(
        codeText = item.code,
        labelText = truncate(item.label, 120),
        pinned = "${item.pinned}",
        locale = item.locale,
        attempts = item.attempts.toString(),
        nameText = truncate(item.name, 80),
        maximumText = "%.2f".format(item.maximum),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeLedgerViews(items: MutableList<TransitTimeLedger>): MutableList<TransitTimeLedgerView> {
    val views = mutableListOf<TransitTimeLedgerView>()
    for (item in items) {
        views.add(toTransitTimeLedgerView(item))
    }
    return views
}

fun sumTransitTimeLedgerView(view: TransitTimeLedgerView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedAttempts.toDouble() + parsedMaximum + parsedPriority.toDouble()
}

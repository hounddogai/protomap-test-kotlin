package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock ledger. */
data class CrossDockLedgerView(
    var codeText: String,
    var title: String,
    var currency: String,
    var ceiling: String,
    var region: String,
    var country: String,
    var ratioText: String,
    var notes: String,
    var contactEmailText: String,
    var source: String,
    var statusLabel: String,
)

fun toCrossDockLedgerView(item: CrossDockLedger): CrossDockLedgerView {
    return CrossDockLedgerView(
        codeText = truncate(item.code, 120),
        title = item.title,
        currency = item.currency,
        ceiling = "%.2f".format(item.ceiling),
        region = item.region,
        country = item.country,
        ratioText = "%.2f".format(item.ratio),
        notes = item.notes,
        contactEmailText = item.contactEmail,
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockLedgerViews(items: MutableList<CrossDockLedger>): MutableList<CrossDockLedgerView> {
    val views = mutableListOf<CrossDockLedgerView>()
    for (item in items) {
        views.add(toCrossDockLedgerView(item))
    }
    return views
}

fun sumCrossDockLedgerView(view: CrossDockLedgerView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedCeiling + parsedRatio
}

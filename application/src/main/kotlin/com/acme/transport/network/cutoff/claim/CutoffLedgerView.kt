package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff ledger. */
data class CutoffLedgerView(
    var code: String,
    var nameText: String,
    var rate: String,
    var priority: String,
    var ratio: String,
    var statusLabel: String,
)

fun toCutoffLedgerView(item: CutoffLedger): CutoffLedgerView {
    return CutoffLedgerView(
        code = item.code,
        nameText = truncate(item.name, 64),
        rate = "%.2f".format(item.rate),
        priority = item.priority.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffLedgerViews(items: MutableList<CutoffLedger>): MutableList<CutoffLedgerView> {
    val views = mutableListOf<CutoffLedgerView>()
    for (item in items) {
        views.add(toCutoffLedgerView(item))
    }
    return views
}

fun sumCutoffLedgerView(view: CutoffLedgerView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedRate + parsedPriority.toDouble() + parsedRatio
}

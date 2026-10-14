package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global backhaul ledger. */
data class GlobalBackhaulLedgerView(
    var code: String,
    var nameText: String,
    var summary: String,
    var countryText: String,
    var maximum: String,
    var loadKgText: String,
    var categoryText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toGlobalBackhaulLedgerView(item: GlobalBackhaulLedger): GlobalBackhaulLedgerView {
    return GlobalBackhaulLedgerView(
        code = truncate(item.code, 120),
        nameText = truncate(item.name, 80),
        summary = item.summary,
        countryText = truncate(item.country, 40),
        maximum = "%.2f".format(item.maximum),
        loadKgText = "%.2f".format(item.loadKg),
        categoryText = item.category,
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalBackhaulLedgerViews(items: MutableList<GlobalBackhaulLedger>): MutableList<GlobalBackhaulLedgerView> {
    val views = mutableListOf<GlobalBackhaulLedgerView>()
    for (item in items) {
        views.add(toGlobalBackhaulLedgerView(item))
    }
    return views
}

fun sumGlobalBackhaulLedgerView(view: GlobalBackhaulLedgerView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedMaximum + parsedLoadKg + parsedRatio
}

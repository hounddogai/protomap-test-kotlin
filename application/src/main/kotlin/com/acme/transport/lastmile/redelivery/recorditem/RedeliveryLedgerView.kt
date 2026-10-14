package com.acme.transport.lastmile.redelivery.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery ledger. */
data class RedeliveryLedgerView(
    var code: String,
    var titleText: String,
    var rate: String,
    var etaMinutes: String,
    var minimum: String,
    var label: String,
    var summary: String,
    var statusLabel: String,
)

fun toRedeliveryLedgerView(item: RedeliveryLedger): RedeliveryLedgerView {
    return RedeliveryLedgerView(
        code = item.code,
        titleText = item.title,
        rate = "%.2f".format(item.rate),
        etaMinutes = item.etaMinutes.toString(),
        minimum = "%.2f".format(item.minimum),
        label = truncate(item.label, 120),
        summary = truncate(item.summary, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryLedgerViews(items: MutableList<RedeliveryLedger>): MutableList<RedeliveryLedgerView> {
    val views = mutableListOf<RedeliveryLedgerView>()
    for (item in items) {
        views.add(toRedeliveryLedgerView(item))
    }
    return views
}

fun sumRedeliveryLedgerView(view: RedeliveryLedgerView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedRate + parsedEtaMinutes.toDouble() + parsedMinimum
}

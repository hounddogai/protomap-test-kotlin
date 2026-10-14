package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional backhaul ledger. */
data class RegionalBackhaulLedgerView(
    var codeText: String,
    var name: String,
    var routeCode: String,
    var segment: String,
    var rate: String,
    var categoryText: String,
    var checksum: String,
    var summary: String,
    var description: String,
    var sequence: String,
    var statusLabel: String,
)

fun toRegionalBackhaulLedgerView(item: RegionalBackhaulLedger): RegionalBackhaulLedgerView {
    return RegionalBackhaulLedgerView(
        codeText = item.code,
        name = truncate(item.name, 80),
        routeCode = truncate(item.routeCode, 120),
        segment = truncate(item.segment, 80),
        rate = "%.2f".format(item.rate),
        categoryText = item.category,
        checksum = item.checksum,
        summary = item.summary,
        description = item.description,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalBackhaulLedgerViews(items: MutableList<RegionalBackhaulLedger>): MutableList<RegionalBackhaulLedgerView> {
    val views = mutableListOf<RegionalBackhaulLedgerView>()
    for (item in items) {
        views.add(toRegionalBackhaulLedgerView(item))
    }
    return views
}

fun sumRegionalBackhaulLedgerView(view: RegionalBackhaulLedgerView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedRate + parsedSequence.toDouble()
}

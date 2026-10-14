package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback cutoff ledger. */
data class FallbackCutoffLedgerView(
    var code: String,
    var title: String,
    var segmentText: String,
    var odometer: String,
    var etaMinutes: String,
    var rate: String,
    var stopCount: String,
    var contactEmail: String,
    var margin: String,
    var statusLabel: String,
)

fun toFallbackCutoffLedgerView(item: FallbackCutoffLedger): FallbackCutoffLedgerView {
    return FallbackCutoffLedgerView(
        code = item.code,
        title = truncate(item.title, 64),
        segmentText = item.segment,
        odometer = item.odometer.toString(),
        etaMinutes = item.etaMinutes.toString(),
        rate = "%.2f".format(item.rate),
        stopCount = item.stopCount.toString(),
        contactEmail = item.contactEmail,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCutoffLedgerViews(items: MutableList<FallbackCutoffLedger>): MutableList<FallbackCutoffLedgerView> {
    val views = mutableListOf<FallbackCutoffLedgerView>()
    for (item in items) {
        views.add(toFallbackCutoffLedgerView(item))
    }
    return views
}

fun sumFallbackCutoffLedgerView(view: FallbackCutoffLedgerView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedOdometer.toDouble() + parsedEtaMinutes.toDouble() + parsedRate
}

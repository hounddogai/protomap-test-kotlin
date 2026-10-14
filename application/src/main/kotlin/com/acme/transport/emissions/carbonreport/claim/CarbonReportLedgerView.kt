package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a carbon report ledger. */
data class CarbonReportLedgerView(
    var codeText: String,
    var label: String,
    var externalRef: String,
    var distanceKmText: String,
    var attempts: String,
    var shiftCode: String,
    var marginText: String,
    var region: String,
    var description: String,
    var statusLabel: String,
)

fun toCarbonReportLedgerView(item: CarbonReportLedger): CarbonReportLedgerView {
    return CarbonReportLedgerView(
        codeText = item.code,
        label = truncate(item.label, 64),
        externalRef = truncate(item.externalRef, 80),
        distanceKmText = "%.2f".format(item.distanceKm),
        attempts = item.attempts.toString(),
        shiftCode = item.shiftCode,
        marginText = "%.2f".format(item.margin),
        region = item.region,
        description = truncate(item.description, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportLedgerViews(items: MutableList<CarbonReportLedger>): MutableList<CarbonReportLedgerView> {
    val views = mutableListOf<CarbonReportLedgerView>()
    for (item in items) {
        views.add(toCarbonReportLedgerView(item))
    }
    return views
}

fun sumCarbonReportLedgerView(view: CarbonReportLedgerView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedDistanceKm + parsedAttempts.toDouble() + parsedMargin
}

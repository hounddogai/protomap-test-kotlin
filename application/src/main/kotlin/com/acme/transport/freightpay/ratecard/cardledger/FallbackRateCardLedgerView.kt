package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback rate card ledger. */
data class FallbackRateCardLedgerView(
    var code: String,
    var title: String,
    var ceiling: String,
    var fuelLiters: String,
    var versionText: String,
    var archivedText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toFallbackRateCardLedgerView(item: FallbackRateCardLedger): FallbackRateCardLedgerView {
    return FallbackRateCardLedgerView(
        code = item.code,
        title = truncate(item.title, 80),
        ceiling = "%.2f".format(item.ceiling),
        fuelLiters = "%.2f".format(item.fuelLiters),
        versionText = item.version.toString(),
        archivedText = "${item.archived}",
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRateCardLedgerViews(items: MutableList<FallbackRateCardLedger>): MutableList<FallbackRateCardLedgerView> {
    val views = mutableListOf<FallbackRateCardLedgerView>()
    for (item in items) {
        views.add(toFallbackRateCardLedgerView(item))
    }
    return views
}

fun sumFallbackRateCardLedgerView(view: FallbackRateCardLedgerView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedCeiling + parsedFuelLiters + parsedVersion.toDouble()
}

package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal service area ledger. */
data class SeasonalServiceAreaLedgerView(
    var codeText: String,
    var title: String,
    var labelText: String,
    var reviewerText: String,
    var carrierText: String,
    var localeText: String,
    var maximum: String,
    var fuelLitersText: String,
    var priority: String,
    var statusLabel: String,
)

fun toSeasonalServiceAreaLedgerView(item: SeasonalServiceAreaLedger): SeasonalServiceAreaLedgerView {
    return SeasonalServiceAreaLedgerView(
        codeText = truncate(item.code, 40),
        title = item.title,
        labelText = item.label,
        reviewerText = item.reviewer,
        carrierText = truncate(item.carrier, 64),
        localeText = item.locale,
        maximum = "%.2f".format(item.maximum),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalServiceAreaLedgerViews(items: MutableList<SeasonalServiceAreaLedger>): MutableList<SeasonalServiceAreaLedgerView> {
    val views = mutableListOf<SeasonalServiceAreaLedgerView>()
    for (item in items) {
        views.add(toSeasonalServiceAreaLedgerView(item))
    }
    return views
}

fun sumSeasonalServiceAreaLedgerView(view: SeasonalServiceAreaLedgerView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedMaximum + parsedFuelLiters + parsedPriority.toDouble()
}

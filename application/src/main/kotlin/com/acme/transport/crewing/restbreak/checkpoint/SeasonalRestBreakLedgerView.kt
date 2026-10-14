package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal rest break ledger. */
data class SeasonalRestBreakLedgerView(
    var code: String,
    var title: String,
    var descriptionText: String,
    var maximum: String,
    var retriesText: String,
    var label: String,
    var minimum: String,
    var statusLabel: String,
)

fun toSeasonalRestBreakLedgerView(item: SeasonalRestBreakLedger): SeasonalRestBreakLedgerView {
    return SeasonalRestBreakLedgerView(
        code = item.code,
        title = truncate(item.title, 120),
        descriptionText = truncate(item.description, 40),
        maximum = "%.2f".format(item.maximum),
        retriesText = item.retries.toString(),
        label = item.label,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRestBreakLedgerViews(items: MutableList<SeasonalRestBreakLedger>): MutableList<SeasonalRestBreakLedgerView> {
    val views = mutableListOf<SeasonalRestBreakLedgerView>()
    for (item in items) {
        views.add(toSeasonalRestBreakLedgerView(item))
    }
    return views
}

fun sumSeasonalRestBreakLedgerView(view: SeasonalRestBreakLedgerView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedRetries = view.retriesText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedMaximum + parsedRetries.toDouble() + parsedMinimum
}

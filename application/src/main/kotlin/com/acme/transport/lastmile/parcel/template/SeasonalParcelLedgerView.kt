package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal parcel ledger. */
data class SeasonalParcelLedgerView(
    var code: String,
    var name: String,
    var timezone: String,
    var pinnedText: String,
    var title: String,
    var threshold: String,
    var amountText: String,
    var lane: String,
    var statusLabel: String,
)

fun toSeasonalParcelLedgerView(item: SeasonalParcelLedger): SeasonalParcelLedgerView {
    return SeasonalParcelLedgerView(
        code = truncate(item.code, 40),
        name = item.name,
        timezone = item.timezone,
        pinnedText = "${item.pinned}",
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        amountText = "%.2f".format(item.amount),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalParcelLedgerViews(items: MutableList<SeasonalParcelLedger>): MutableList<SeasonalParcelLedgerView> {
    val views = mutableListOf<SeasonalParcelLedgerView>()
    for (item in items) {
        views.add(toSeasonalParcelLedgerView(item))
    }
    return views
}

fun sumSeasonalParcelLedgerView(view: SeasonalParcelLedgerView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedThreshold + parsedAmount
}

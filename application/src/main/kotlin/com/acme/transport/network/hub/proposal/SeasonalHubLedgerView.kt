package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal hub ledger. */
data class SeasonalHubLedgerView(
    var code: String,
    var name: String,
    var notes: String,
    var quantity: String,
    var lane: String,
    var startsAt: String,
    var label: String,
    var statusLabel: String,
)

fun toSeasonalHubLedgerView(item: SeasonalHubLedger): SeasonalHubLedgerView {
    return SeasonalHubLedgerView(
        code = item.code,
        name = truncate(item.name, 40),
        notes = truncate(item.notes, 40),
        quantity = item.quantity.toString(),
        lane = truncate(item.lane, 120),
        startsAt = item.startsAt.toString(),
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalHubLedgerViews(items: MutableList<SeasonalHubLedger>): MutableList<SeasonalHubLedgerView> {
    val views = mutableListOf<SeasonalHubLedgerView>()
    for (item in items) {
        views.add(toSeasonalHubLedgerView(item))
    }
    return views
}

fun sumSeasonalHubLedgerView(view: SeasonalHubLedgerView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedQuantity.toDouble() + parsedStartsAt.toDouble()
}

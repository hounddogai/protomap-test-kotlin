package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal zone policy. */
data class SeasonalZonePolicyView(
    var codeText: String,
    var name: String,
    var activeText: String,
    var summary: String,
    var routeCode: String,
    var trailerId: String,
    var amount: String,
    var statusLabel: String,
)

fun toSeasonalZonePolicyView(item: SeasonalZonePolicy): SeasonalZonePolicyView {
    return SeasonalZonePolicyView(
        codeText = item.code,
        name = item.name,
        activeText = "${item.active}",
        summary = truncate(item.summary, 120),
        routeCode = truncate(item.routeCode, 64),
        trailerId = item.trailerId,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalZonePolicyViews(items: MutableList<SeasonalZonePolicy>): MutableList<SeasonalZonePolicyView> {
    val views = mutableListOf<SeasonalZonePolicyView>()
    for (item in items) {
        views.add(toSeasonalZonePolicyView(item))
    }
    return views
}

fun sumSeasonalZonePolicyView(view: SeasonalZonePolicyView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}

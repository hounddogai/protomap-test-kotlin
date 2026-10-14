package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional route offset entry. */
data class RegionalRouteOffsetEntryView(
    var codeText: String,
    var nameText: String,
    var pinned: String,
    var sourceText: String,
    var endsAtText: String,
    var fuelLitersText: String,
    var trailerId: String,
    var amountText: String,
    var unit: String,
    var statusLabel: String,
)

fun toRegionalRouteOffsetEntryView(item: RegionalRouteOffsetEntry): RegionalRouteOffsetEntryView {
    return RegionalRouteOffsetEntryView(
        codeText = item.code,
        nameText = item.name,
        pinned = "${item.pinned}",
        sourceText = truncate(item.source, 64),
        endsAtText = item.endsAt.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        trailerId = item.trailerId,
        amountText = "%.2f".format(item.amount),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRouteOffsetEntryViews(items: MutableList<RegionalRouteOffsetEntry>): MutableList<RegionalRouteOffsetEntryView> {
    val views = mutableListOf<RegionalRouteOffsetEntryView>()
    for (item in items) {
        views.add(toRegionalRouteOffsetEntryView(item))
    }
    return views
}

fun sumRegionalRouteOffsetEntryView(view: RegionalRouteOffsetEntryView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedAmount = view.amountText.toDouble()
    return parsedEndsAt.toDouble() + parsedFuelLiters + parsedAmount
}

package com.acme.transport.crewing.roster.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional roster segment. */
data class RegionalRosterSegmentView(
    var code: String,
    var nameText: String,
    var archived: String,
    var pallets: String,
    var quantity: String,
    var dock: String,
    var expiresAt: String,
    var limitText: String,
    var summary: String,
    var statusLabel: String,
)

fun toRegionalRosterSegmentView(item: RegionalRosterSegment): RegionalRosterSegmentView {
    return RegionalRosterSegmentView(
        code = item.code,
        nameText = truncate(item.name, 64),
        archived = "${item.archived}",
        pallets = item.pallets.toString(),
        quantity = item.quantity.toString(),
        dock = item.dock,
        expiresAt = item.expiresAt.toString(),
        limitText = item.limit.toString(),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRosterSegmentViews(items: MutableList<RegionalRosterSegment>): MutableList<RegionalRosterSegmentView> {
    val views = mutableListOf<RegionalRosterSegmentView>()
    for (item in items) {
        views.add(toRegionalRosterSegmentView(item))
    }
    return views
}

fun sumRegionalRosterSegmentView(view: RegionalRosterSegmentView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedPallets.toDouble() + parsedQuantity.toDouble() + parsedExpiresAt.toDouble()
}

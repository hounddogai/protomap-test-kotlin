package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time summary. */
data class TransitTimeSummaryView(
    var code: String,
    var name: String,
    var updatedAt: String,
    var ceiling: String,
    var title: String,
    var pallets: String,
    var country: String,
    var categoryText: String,
    var trailerId: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toTransitTimeSummaryView(item: TransitTimeSummary): TransitTimeSummaryView {
    return TransitTimeSummaryView(
        code = item.code,
        name = truncate(item.name, 64),
        updatedAt = item.updatedAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        title = truncate(item.title, 40),
        pallets = item.pallets.toString(),
        country = item.country,
        categoryText = item.category,
        trailerId = truncate(item.trailerId, 80),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeSummaryViews(items: MutableList<TransitTimeSummary>): MutableList<TransitTimeSummaryView> {
    val views = mutableListOf<TransitTimeSummaryView>()
    for (item in items) {
        views.add(toTransitTimeSummaryView(item))
    }
    return views
}

fun sumTransitTimeSummaryView(view: TransitTimeSummaryView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedUpdatedAt.toDouble() + parsedCeiling + parsedPallets.toDouble()
}

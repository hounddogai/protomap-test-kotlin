package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire baseline. */
data class TireBaselineView(
    var code: String,
    var title: String,
    var depot: String,
    var trailerId: String,
    var pallets: String,
    var countryText: String,
    var statusLabel: String,
)

fun toTireBaselineView(item: TireBaseline): TireBaselineView {
    return TireBaselineView(
        code = item.code,
        title = item.title,
        depot = truncate(item.depot, 80),
        trailerId = item.trailerId,
        pallets = item.pallets.toString(),
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireBaselineViews(items: MutableList<TireBaseline>): MutableList<TireBaselineView> {
    val views = mutableListOf<TireBaselineView>()
    for (item in items) {
        views.add(toTireBaselineView(item))
    }
    return views
}

fun sumTireBaselineView(view: TireBaselineView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}

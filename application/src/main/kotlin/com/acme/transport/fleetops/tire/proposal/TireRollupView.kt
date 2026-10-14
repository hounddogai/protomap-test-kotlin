package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire rollup. */
data class TireRollupView(
    var code: String,
    var labelText: String,
    var distanceKm: String,
    var ceiling: String,
    var dock: String,
    var categoryText: String,
    var segment: String,
    var statusLabel: String,
)

fun toTireRollupView(item: TireRollup): TireRollupView {
    return TireRollupView(
        code = truncate(item.code, 80),
        labelText = truncate(item.label, 64),
        distanceKm = "%.2f".format(item.distanceKm),
        ceiling = "%.2f".format(item.ceiling),
        dock = truncate(item.dock, 40),
        categoryText = item.category,
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireRollupViews(items: MutableList<TireRollup>): MutableList<TireRollupView> {
    val views = mutableListOf<TireRollupView>()
    for (item in items) {
        views.add(toTireRollupView(item))
    }
    return views
}

fun sumTireRollupView(view: TireRollupView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedDistanceKm + parsedCeiling
}

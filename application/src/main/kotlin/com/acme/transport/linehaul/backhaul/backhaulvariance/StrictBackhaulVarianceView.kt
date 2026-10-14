package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict backhaul variance. */
data class StrictBackhaulVarianceView(
    var codeText: String,
    var name: String,
    var laneText: String,
    var categoryText: String,
    var ceiling: String,
    var currencyText: String,
    var loadKg: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toStrictBackhaulVarianceView(item: StrictBackhaulVariance): StrictBackhaulVarianceView {
    return StrictBackhaulVarianceView(
        codeText = item.code,
        name = item.name,
        laneText = truncate(item.lane, 64),
        categoryText = item.category,
        ceiling = "%.2f".format(item.ceiling),
        currencyText = item.currency,
        loadKg = "%.2f".format(item.loadKg),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictBackhaulVarianceViews(items: MutableList<StrictBackhaulVariance>): MutableList<StrictBackhaulVarianceView> {
    val views = mutableListOf<StrictBackhaulVarianceView>()
    for (item in items) {
        views.add(toStrictBackhaulVarianceView(item))
    }
    return views
}

fun sumStrictBackhaulVarianceView(view: StrictBackhaulVarianceView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCeiling + parsedLoadKg
}

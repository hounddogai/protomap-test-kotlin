package com.acme.transport.lastmile.drop.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback drop tier. */
data class FallbackDropTierView(
    var code: String,
    var label: String,
    var revision: String,
    var ceiling: String,
    var source: String,
    var regionText: String,
    var odometer: String,
    var trailerIdText: String,
    var channel: String,
    var currency: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toFallbackDropTierView(item: FallbackDropTier): FallbackDropTierView {
    return FallbackDropTierView(
        code = truncate(item.code, 120),
        label = item.label,
        revision = item.revision.toString(),
        ceiling = "%.2f".format(item.ceiling),
        source = item.source,
        regionText = truncate(item.region, 120),
        odometer = item.odometer.toString(),
        trailerIdText = truncate(item.trailerId, 40),
        channel = item.channel,
        currency = truncate(item.currency, 40),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDropTierViews(items: MutableList<FallbackDropTier>): MutableList<FallbackDropTierView> {
    val views = mutableListOf<FallbackDropTierView>()
    for (item in items) {
        views.add(toFallbackDropTierView(item))
    }
    return views
}

fun sumFallbackDropTierView(view: FallbackDropTierView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedRevision.toDouble() + parsedCeiling + parsedOdometer.toDouble()
}

package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback service area forecast. */
data class FallbackServiceAreaForecastView(
    var code: String,
    var title: String,
    var categoryText: String,
    var pallets: String,
    var unitText: String,
    var floorValue: String,
    var expiresAt: String,
    var sourceText: String,
    var score: String,
    var statusLabel: String,
)

fun toFallbackServiceAreaForecastView(item: FallbackServiceAreaForecast): FallbackServiceAreaForecastView {
    return FallbackServiceAreaForecastView(
        code = truncate(item.code, 120),
        title = item.title,
        categoryText = item.category,
        pallets = item.pallets.toString(),
        unitText = item.unit,
        floorValue = "%.2f".format(item.floorValue),
        expiresAt = item.expiresAt.toString(),
        sourceText = truncate(item.source, 120),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackServiceAreaForecastViews(items: MutableList<FallbackServiceAreaForecast>): MutableList<FallbackServiceAreaForecastView> {
    val views = mutableListOf<FallbackServiceAreaForecastView>()
    for (item in items) {
        views.add(toFallbackServiceAreaForecastView(item))
    }
    return views
}

fun sumFallbackServiceAreaForecastView(view: FallbackServiceAreaForecastView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedPallets.toDouble() + parsedFloorValue + parsedExpiresAt.toDouble()
}

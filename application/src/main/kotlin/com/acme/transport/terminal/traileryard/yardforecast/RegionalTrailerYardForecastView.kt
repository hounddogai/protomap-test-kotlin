package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional trailer yard forecast. */
data class RegionalTrailerYardForecastView(
    var code: String,
    var title: String,
    var ceilingText: String,
    var threshold: String,
    var pinned: String,
    var weightText: String,
    var categoryText: String,
    var descriptionText: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toRegionalTrailerYardForecastView(item: RegionalTrailerYardForecast): RegionalTrailerYardForecastView {
    return RegionalTrailerYardForecastView(
        code = item.code,
        title = truncate(item.title, 120),
        ceilingText = "%.2f".format(item.ceiling),
        threshold = "%.2f".format(item.threshold),
        pinned = "${item.pinned}",
        weightText = "%.2f".format(item.weight),
        categoryText = item.category,
        descriptionText = item.description,
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalTrailerYardForecastViews(items: MutableList<RegionalTrailerYardForecast>): MutableList<RegionalTrailerYardForecastView> {
    val views = mutableListOf<RegionalTrailerYardForecastView>()
    for (item in items) {
        views.add(toRegionalTrailerYardForecastView(item))
    }
    return views
}

fun sumRegionalTrailerYardForecastView(view: RegionalTrailerYardForecastView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedCeiling + parsedThreshold + parsedWeight
}

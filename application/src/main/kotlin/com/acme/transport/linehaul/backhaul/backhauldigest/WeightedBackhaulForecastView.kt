package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted backhaul forecast. */
data class WeightedBackhaulForecastView(
    var code: String,
    var titleText: String,
    var margin: String,
    var reviewer: String,
    var name: String,
    var statusLabel: String,
)

fun toWeightedBackhaulForecastView(item: WeightedBackhaulForecast): WeightedBackhaulForecastView {
    return WeightedBackhaulForecastView(
        code = item.code,
        titleText = truncate(item.title, 64),
        margin = "%.2f".format(item.margin),
        reviewer = item.reviewer,
        name = truncate(item.name, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedBackhaulForecastViews(items: MutableList<WeightedBackhaulForecast>): MutableList<WeightedBackhaulForecastView> {
    val views = mutableListOf<WeightedBackhaulForecastView>()
    for (item in items) {
        views.add(toWeightedBackhaulForecastView(item))
    }
    return views
}

fun sumWeightedBackhaulForecastView(view: WeightedBackhaulForecastView): Double {
    val parsedMargin = view.margin.toDouble()
    return parsedMargin
}

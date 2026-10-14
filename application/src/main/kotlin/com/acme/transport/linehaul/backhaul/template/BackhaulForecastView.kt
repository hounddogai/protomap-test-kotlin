package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul forecast. */
data class BackhaulForecastView(
    var code: String,
    var titleText: String,
    var reviewerText: String,
    var segment: String,
    var rate: String,
    var labelText: String,
    var weight: String,
    var stopCount: String,
    var laneText: String,
    var statusLabel: String,
)

fun toBackhaulForecastView(item: BackhaulForecast): BackhaulForecastView {
    return BackhaulForecastView(
        code = truncate(item.code, 64),
        titleText = item.title,
        reviewerText = item.reviewer,
        segment = item.segment,
        rate = "%.2f".format(item.rate),
        labelText = item.label,
        weight = "%.2f".format(item.weight),
        stopCount = item.stopCount.toString(),
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulForecastViews(items: MutableList<BackhaulForecast>): MutableList<BackhaulForecastView> {
    val views = mutableListOf<BackhaulForecastView>()
    for (item in items) {
        views.add(toBackhaulForecastView(item))
    }
    return views
}

fun sumBackhaulForecastView(view: BackhaulForecastView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedRate + parsedWeight + parsedStopCount.toDouble()
}

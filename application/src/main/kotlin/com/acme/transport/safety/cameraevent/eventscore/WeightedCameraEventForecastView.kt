package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted camera event forecast. */
data class WeightedCameraEventForecastView(
    var codeText: String,
    var title: String,
    var timezone: String,
    var createdAtText: String,
    var score: String,
    var loadKg: String,
    var ratio: String,
    var windowDays: String,
    var maximumText: String,
    var durationMinutesText: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toWeightedCameraEventForecastView(item: WeightedCameraEventForecast): WeightedCameraEventForecastView {
    return WeightedCameraEventForecastView(
        codeText = item.code,
        title = item.title,
        timezone = truncate(item.timezone, 120),
        createdAtText = item.createdAt.toString(),
        score = "%.2f".format(item.score),
        loadKg = "%.2f".format(item.loadKg),
        ratio = "%.2f".format(item.ratio),
        windowDays = item.windowDays.toString(),
        maximumText = "%.2f".format(item.maximum),
        durationMinutesText = item.durationMinutes.toString(),
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedCameraEventForecastViews(items: MutableList<WeightedCameraEventForecast>): MutableList<WeightedCameraEventForecastView> {
    val views = mutableListOf<WeightedCameraEventForecastView>()
    for (item in items) {
        views.add(toWeightedCameraEventForecastView(item))
    }
    return views
}

fun sumWeightedCameraEventForecastView(view: WeightedCameraEventForecastView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedScore = view.score.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCreatedAt.toDouble() + parsedScore + parsedLoadKg
}

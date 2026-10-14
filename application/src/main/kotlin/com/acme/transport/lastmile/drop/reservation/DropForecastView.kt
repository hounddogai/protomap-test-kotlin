package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop forecast. */
data class DropForecastView(
    var code: String,
    var titleText: String,
    var country: String,
    var updatedAt: String,
    var notes: String,
    var version: String,
    var startsAtText: String,
    var score: String,
    var thresholdText: String,
    var currency: String,
    var summary: String,
    var statusLabel: String,
)

fun toDropForecastView(item: DropForecast): DropForecastView {
    return DropForecastView(
        code = item.code,
        titleText = truncate(item.title, 120),
        country = item.country,
        updatedAt = item.updatedAt.toString(),
        notes = item.notes,
        version = item.version.toString(),
        startsAtText = item.startsAt.toString(),
        score = "%.2f".format(item.score),
        thresholdText = "%.2f".format(item.threshold),
        currency = item.currency,
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropForecastViews(items: MutableList<DropForecast>): MutableList<DropForecastView> {
    val views = mutableListOf<DropForecastView>()
    for (item in items) {
        views.add(toDropForecastView(item))
    }
    return views
}

fun sumDropForecastView(view: DropForecastView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedVersion = view.version.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedUpdatedAt.toDouble() + parsedVersion.toDouble() + parsedStartsAt.toDouble()
}

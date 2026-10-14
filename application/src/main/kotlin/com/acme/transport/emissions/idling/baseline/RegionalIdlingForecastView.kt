package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional idling forecast. */
data class RegionalIdlingForecastView(
    var code: String,
    var titleText: String,
    var shiftCode: String,
    var archived: String,
    var ownerId: String,
    var score: String,
    var statusLabel: String,
)

fun toRegionalIdlingForecastView(item: RegionalIdlingForecast): RegionalIdlingForecastView {
    return RegionalIdlingForecastView(
        code = truncate(item.code, 40),
        titleText = item.title,
        shiftCode = item.shiftCode,
        archived = "${item.archived}",
        ownerId = item.ownerId,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalIdlingForecastViews(items: MutableList<RegionalIdlingForecast>): MutableList<RegionalIdlingForecastView> {
    val views = mutableListOf<RegionalIdlingForecastView>()
    for (item in items) {
        views.add(toRegionalIdlingForecastView(item))
    }
    return views
}

fun sumRegionalIdlingForecastView(view: RegionalIdlingForecastView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}

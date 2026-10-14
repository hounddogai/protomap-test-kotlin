package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy idling forecast. */
data class LegacyIdlingForecastView(
    var code: String,
    var title: String,
    var vehicleId: String,
    var endsAt: String,
    var source: String,
    var limit: String,
    var sequence: String,
    var statusLabel: String,
)

fun toLegacyIdlingForecastView(item: LegacyIdlingForecast): LegacyIdlingForecastView {
    return LegacyIdlingForecastView(
        code = item.code,
        title = truncate(item.title, 40),
        vehicleId = item.vehicleId,
        endsAt = item.endsAt.toString(),
        source = item.source,
        limit = item.limit.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyIdlingForecastViews(items: MutableList<LegacyIdlingForecast>): MutableList<LegacyIdlingForecastView> {
    val views = mutableListOf<LegacyIdlingForecastView>()
    for (item in items) {
        views.add(toLegacyIdlingForecastView(item))
    }
    return views
}

fun sumLegacyIdlingForecastView(view: LegacyIdlingForecastView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedEndsAt.toDouble() + parsedLimit.toDouble() + parsedSequence.toDouble()
}

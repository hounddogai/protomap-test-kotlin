package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled incident forecast. */
data class ScheduledIncidentForecastView(
    var code: String,
    var name: String,
    var retriesText: String,
    var externalRef: String,
    var title: String,
    var carrierText: String,
    var summary: String,
    var timezone: String,
    var statusLabel: String,
)

fun toScheduledIncidentForecastView(item: ScheduledIncidentForecast): ScheduledIncidentForecastView {
    return ScheduledIncidentForecastView(
        code = truncate(item.code, 40),
        name = item.name,
        retriesText = item.retries.toString(),
        externalRef = truncate(item.externalRef, 120),
        title = item.title,
        carrierText = truncate(item.carrier, 64),
        summary = item.summary,
        timezone = truncate(item.timezone, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledIncidentForecastViews(items: MutableList<ScheduledIncidentForecast>): MutableList<ScheduledIncidentForecastView> {
    val views = mutableListOf<ScheduledIncidentForecastView>()
    for (item in items) {
        views.add(toScheduledIncidentForecastView(item))
    }
    return views
}

fun sumScheduledIncidentForecastView(view: ScheduledIncidentForecastView): Double {
    val parsedRetries = view.retriesText.toInt()
    return parsedRetries.toDouble()
}

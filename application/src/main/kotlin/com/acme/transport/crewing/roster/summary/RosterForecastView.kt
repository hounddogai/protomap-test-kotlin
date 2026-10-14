package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster forecast. */
data class RosterForecastView(
    var code: String,
    var label: String,
    var laneText: String,
    var priorityText: String,
    var locale: String,
    var driverId: String,
    var statusLabel: String,
)

fun toRosterForecastView(item: RosterForecast): RosterForecastView {
    return RosterForecastView(
        code = item.code,
        label = item.label,
        laneText = item.lane,
        priorityText = item.priority.toString(),
        locale = truncate(item.locale, 40),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterForecastViews(items: MutableList<RosterForecast>): MutableList<RosterForecastView> {
    val views = mutableListOf<RosterForecastView>()
    for (item in items) {
        views.add(toRosterForecastView(item))
    }
    return views
}

fun sumRosterForecastView(view: RosterForecastView): Double {
    val parsedPriority = view.priorityText.toInt()
    return parsedPriority.toDouble()
}

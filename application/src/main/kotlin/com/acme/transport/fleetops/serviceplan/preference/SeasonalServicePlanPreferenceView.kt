package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal service plan preference. */
data class SeasonalServicePlanPreferenceView(
    var codeText: String,
    var label: String,
    var routeCode: String,
    var windowDays: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toSeasonalServicePlanPreferenceView(item: SeasonalServicePlanPreference): SeasonalServicePlanPreferenceView {
    return SeasonalServicePlanPreferenceView(
        codeText = item.code,
        label = item.label,
        routeCode = truncate(item.routeCode, 120),
        windowDays = item.windowDays.toString(),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalServicePlanPreferenceViews(items: MutableList<SeasonalServicePlanPreference>): MutableList<SeasonalServicePlanPreferenceView> {
    val views = mutableListOf<SeasonalServicePlanPreferenceView>()
    for (item in items) {
        views.add(toSeasonalServicePlanPreferenceView(item))
    }
    return views
}

fun sumSeasonalServicePlanPreferenceView(view: SeasonalServicePlanPreferenceView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedWindowDays.toDouble() + parsedMinimum
}

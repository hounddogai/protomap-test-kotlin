package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict incident metric. */
data class StrictIncidentMetricView(
    var codeText: String,
    var labelText: String,
    var graceDaysText: String,
    var shiftCodeText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toStrictIncidentMetricView(item: StrictIncidentMetric): StrictIncidentMetricView {
    return StrictIncidentMetricView(
        codeText = item.code,
        labelText = item.label,
        graceDaysText = item.graceDays.toString(),
        shiftCodeText = truncate(item.shiftCode, 40),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictIncidentMetricViews(items: MutableList<StrictIncidentMetric>): MutableList<StrictIncidentMetricView> {
    val views = mutableListOf<StrictIncidentMetricView>()
    for (item in items) {
        views.add(toStrictIncidentMetricView(item))
    }
    return views
}

fun sumStrictIncidentMetricView(view: StrictIncidentMetricView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedGraceDays.toDouble() + parsedEtaMinutes.toDouble()
}

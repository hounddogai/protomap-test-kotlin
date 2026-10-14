package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy incident metric. */
data class LegacyIncidentMetricView(
    var code: String,
    var titleText: String,
    var activeText: String,
    var pinnedText: String,
    var minimumText: String,
    var label: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toLegacyIncidentMetricView(item: LegacyIncidentMetric): LegacyIncidentMetricView {
    return LegacyIncidentMetricView(
        code = truncate(item.code, 40),
        titleText = truncate(item.title, 80),
        activeText = "${item.active}",
        pinnedText = "${item.pinned}",
        minimumText = "%.2f".format(item.minimum),
        label = item.label,
        trailerIdText = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyIncidentMetricViews(items: MutableList<LegacyIncidentMetric>): MutableList<LegacyIncidentMetricView> {
    val views = mutableListOf<LegacyIncidentMetricView>()
    for (item in items) {
        views.add(toLegacyIncidentMetricView(item))
    }
    return views
}

fun sumLegacyIncidentMetricView(view: LegacyIncidentMetricView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    return parsedMinimum
}

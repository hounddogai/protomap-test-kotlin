package com.acme.transport.safety.incident.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident preference. */
data class IncidentPreferenceView(
    var codeText: String,
    var title: String,
    var windowDays: String,
    var maximum: String,
    var dock: String,
    var trailerId: String,
    var ratio: String,
    var category: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toIncidentPreferenceView(item: IncidentPreference): IncidentPreferenceView {
    return IncidentPreferenceView(
        codeText = truncate(item.code, 80),
        title = item.title,
        windowDays = item.windowDays.toString(),
        maximum = "%.2f".format(item.maximum),
        dock = item.dock,
        trailerId = item.trailerId,
        ratio = "%.2f".format(item.ratio),
        category = item.category,
        reviewerText = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentPreferenceViews(items: MutableList<IncidentPreference>): MutableList<IncidentPreferenceView> {
    val views = mutableListOf<IncidentPreferenceView>()
    for (item in items) {
        views.add(toIncidentPreferenceView(item))
    }
    return views
}

fun sumIncidentPreferenceView(view: IncidentPreferenceView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedWindowDays.toDouble() + parsedMaximum + parsedRatio
}

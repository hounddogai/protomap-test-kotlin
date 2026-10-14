package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal incident milestone. */
data class SeasonalIncidentMilestoneView(
    var codeText: String,
    var label: String,
    var notes: String,
    var loadKg: String,
    var timezone: String,
    var createdAt: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toSeasonalIncidentMilestoneView(item: SeasonalIncidentMilestone): SeasonalIncidentMilestoneView {
    return SeasonalIncidentMilestoneView(
        codeText = truncate(item.code, 80),
        label = item.label,
        notes = item.notes,
        loadKg = "%.2f".format(item.loadKg),
        timezone = truncate(item.timezone, 40),
        createdAt = item.createdAt.toString(),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalIncidentMilestoneViews(items: MutableList<SeasonalIncidentMilestone>): MutableList<SeasonalIncidentMilestoneView> {
    val views = mutableListOf<SeasonalIncidentMilestoneView>()
    for (item in items) {
        views.add(toSeasonalIncidentMilestoneView(item))
    }
    return views
}

fun sumSeasonalIncidentMilestoneView(view: SeasonalIncidentMilestoneView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedLoadKg + parsedCreatedAt.toDouble() + parsedStopCount.toDouble()
}

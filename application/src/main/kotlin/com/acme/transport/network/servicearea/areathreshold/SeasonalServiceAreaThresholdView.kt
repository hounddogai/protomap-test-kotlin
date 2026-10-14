package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal service area threshold. */
data class SeasonalServiceAreaThresholdView(
    var code: String,
    var nameText: String,
    var regionText: String,
    var priority: String,
    var driverId: String,
    var createdAtText: String,
    var notesText: String,
    var statusLabel: String,
)

fun toSeasonalServiceAreaThresholdView(item: SeasonalServiceAreaThreshold): SeasonalServiceAreaThresholdView {
    return SeasonalServiceAreaThresholdView(
        code = item.code,
        nameText = item.name,
        regionText = item.region,
        priority = item.priority.toString(),
        driverId = truncate(item.driverId, 40),
        createdAtText = item.createdAt.toString(),
        notesText = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalServiceAreaThresholdViews(items: MutableList<SeasonalServiceAreaThreshold>): MutableList<SeasonalServiceAreaThresholdView> {
    val views = mutableListOf<SeasonalServiceAreaThresholdView>()
    for (item in items) {
        views.add(toSeasonalServiceAreaThresholdView(item))
    }
    return views
}

fun sumSeasonalServiceAreaThresholdView(view: SeasonalServiceAreaThresholdView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedPriority.toDouble() + parsedCreatedAt.toDouble()
}

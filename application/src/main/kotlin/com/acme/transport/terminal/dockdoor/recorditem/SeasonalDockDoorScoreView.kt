package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal dock door score. */
data class SeasonalDockDoorScoreView(
    var code: String,
    var nameText: String,
    var summary: String,
    var createdAt: String,
    var distanceKm: String,
    var contactEmail: String,
    var driverId: String,
    var statusLabel: String,
)

fun toSeasonalDockDoorScoreView(item: SeasonalDockDoorScore): SeasonalDockDoorScoreView {
    return SeasonalDockDoorScoreView(
        code = item.code,
        nameText = truncate(item.name, 120),
        summary = truncate(item.summary, 120),
        createdAt = item.createdAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        contactEmail = item.contactEmail,
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDockDoorScoreViews(items: MutableList<SeasonalDockDoorScore>): MutableList<SeasonalDockDoorScoreView> {
    val views = mutableListOf<SeasonalDockDoorScoreView>()
    for (item in items) {
        views.add(toSeasonalDockDoorScoreView(item))
    }
    return views
}

fun sumSeasonalDockDoorScoreView(view: SeasonalDockDoorScoreView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedCreatedAt.toDouble() + parsedDistanceKm
}

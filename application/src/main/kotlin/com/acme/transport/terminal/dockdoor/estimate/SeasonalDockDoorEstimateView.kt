package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal dock door estimate. */
data class SeasonalDockDoorEstimateView(
    var code: String,
    var titleText: String,
    var vehicleIdText: String,
    var pinned: String,
    var regionText: String,
    var phoneText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toSeasonalDockDoorEstimateView(item: SeasonalDockDoorEstimate): SeasonalDockDoorEstimateView {
    return SeasonalDockDoorEstimateView(
        code = truncate(item.code, 40),
        titleText = item.title,
        vehicleIdText = item.vehicleId,
        pinned = "${item.pinned}",
        regionText = item.region,
        phoneText = truncate(item.phone, 80),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDockDoorEstimateViews(items: MutableList<SeasonalDockDoorEstimate>): MutableList<SeasonalDockDoorEstimateView> {
    val views = mutableListOf<SeasonalDockDoorEstimateView>()
    for (item in items) {
        views.add(toSeasonalDockDoorEstimateView(item))
    }
    return views
}

fun sumSeasonalDockDoorEstimateView(view: SeasonalDockDoorEstimateView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}

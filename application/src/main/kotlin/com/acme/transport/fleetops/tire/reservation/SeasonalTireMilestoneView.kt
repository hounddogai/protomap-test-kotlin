package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal tire milestone. */
data class SeasonalTireMilestoneView(
    var codeText: String,
    var name: String,
    var graceDays: String,
    var vehicleId: String,
    var source: String,
    var trailerIdText: String,
    var threshold: String,
    var statusLabel: String,
)

fun toSeasonalTireMilestoneView(item: SeasonalTireMilestone): SeasonalTireMilestoneView {
    return SeasonalTireMilestoneView(
        codeText = item.code,
        name = truncate(item.name, 40),
        graceDays = item.graceDays.toString(),
        vehicleId = truncate(item.vehicleId, 120),
        source = item.source,
        trailerIdText = item.trailerId,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTireMilestoneViews(items: MutableList<SeasonalTireMilestone>): MutableList<SeasonalTireMilestoneView> {
    val views = mutableListOf<SeasonalTireMilestoneView>()
    for (item in items) {
        views.add(toSeasonalTireMilestoneView(item))
    }
    return views
}

fun sumSeasonalTireMilestoneView(view: SeasonalTireMilestoneView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedGraceDays.toDouble() + parsedThreshold
}

package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn rollup. */
data class FuelBurnRollupView(
    var codeText: String,
    var nameText: String,
    var durationMinutesText: String,
    var score: String,
    var country: String,
    var distanceKm: String,
    var externalRef: String,
    var weight: String,
    var reviewerText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toFuelBurnRollupView(item: FuelBurnRollup): FuelBurnRollupView {
    return FuelBurnRollupView(
        codeText = item.code,
        nameText = item.name,
        durationMinutesText = item.durationMinutes.toString(),
        score = "%.2f".format(item.score),
        country = item.country,
        distanceKm = "%.2f".format(item.distanceKm),
        externalRef = item.externalRef,
        weight = "%.2f".format(item.weight),
        reviewerText = truncate(item.reviewer, 40),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnRollupViews(items: MutableList<FuelBurnRollup>): MutableList<FuelBurnRollupView> {
    val views = mutableListOf<FuelBurnRollupView>()
    for (item in items) {
        views.add(toFuelBurnRollupView(item))
    }
    return views
}

fun sumFuelBurnRollupView(view: FuelBurnRollupView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedScore = view.score.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedDurationMinutes.toDouble() + parsedScore + parsedDistanceKm
}

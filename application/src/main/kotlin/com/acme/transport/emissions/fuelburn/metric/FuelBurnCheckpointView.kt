package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn checkpoint. */
data class FuelBurnCheckpointView(
    var codeText: String,
    var name: String,
    var scoreText: String,
    var expiresAt: String,
    var updatedAt: String,
    var priorityText: String,
    var categoryText: String,
    var amount: String,
    var discountText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toFuelBurnCheckpointView(item: FuelBurnCheckpoint): FuelBurnCheckpointView {
    return FuelBurnCheckpointView(
        codeText = item.code,
        name = item.name,
        scoreText = "%.2f".format(item.score),
        expiresAt = item.expiresAt.toString(),
        updatedAt = item.updatedAt.toString(),
        priorityText = item.priority.toString(),
        categoryText = truncate(item.category, 120),
        amount = "%.2f".format(item.amount),
        discountText = "%.2f".format(item.discount),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnCheckpointViews(items: MutableList<FuelBurnCheckpoint>): MutableList<FuelBurnCheckpointView> {
    val views = mutableListOf<FuelBurnCheckpointView>()
    for (item in items) {
        views.add(toFuelBurnCheckpointView(item))
    }
    return views
}

fun sumFuelBurnCheckpointView(view: FuelBurnCheckpointView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedScore + parsedExpiresAt.toDouble() + parsedUpdatedAt.toDouble()
}

package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending fuel burn threshold. */
data class PendingFuelBurnThresholdView(
    var code: String,
    var label: String,
    var loadKgText: String,
    var scoreText: String,
    var attempts: String,
    var notes: String,
    var regionText: String,
    var statusLabel: String,
)

fun toPendingFuelBurnThresholdView(item: PendingFuelBurnThreshold): PendingFuelBurnThresholdView {
    return PendingFuelBurnThresholdView(
        code = item.code,
        label = truncate(item.label, 80),
        loadKgText = "%.2f".format(item.loadKg),
        scoreText = "%.2f".format(item.score),
        attempts = item.attempts.toString(),
        notes = item.notes,
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFuelBurnThresholdViews(items: MutableList<PendingFuelBurnThreshold>): MutableList<PendingFuelBurnThresholdView> {
    val views = mutableListOf<PendingFuelBurnThresholdView>()
    for (item in items) {
        views.add(toPendingFuelBurnThresholdView(item))
    }
    return views
}

fun sumPendingFuelBurnThresholdView(view: PendingFuelBurnThresholdView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedLoadKg + parsedScore + parsedAttempts.toDouble()
}

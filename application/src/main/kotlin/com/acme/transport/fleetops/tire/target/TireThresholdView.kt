package com.acme.transport.fleetops.tire.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire threshold. */
data class TireThresholdView(
    var code: String,
    var titleText: String,
    var expiresAt: String,
    var vehicleId: String,
    var score: String,
    var maximum: String,
    var carrier: String,
    var checksum: String,
    var capacity: String,
    var etaMinutesText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toTireThresholdView(item: TireThreshold): TireThresholdView {
    return TireThresholdView(
        code = truncate(item.code, 120),
        titleText = item.title,
        expiresAt = item.expiresAt.toString(),
        vehicleId = item.vehicleId,
        score = "%.2f".format(item.score),
        maximum = "%.2f".format(item.maximum),
        carrier = item.carrier,
        checksum = item.checksum,
        capacity = item.capacity.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireThresholdViews(items: MutableList<TireThreshold>): MutableList<TireThresholdView> {
    val views = mutableListOf<TireThresholdView>()
    for (item in items) {
        views.add(toTireThresholdView(item))
    }
    return views
}

fun sumTireThresholdView(view: TireThresholdView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedScore = view.score.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedExpiresAt.toDouble() + parsedScore + parsedMaximum
}

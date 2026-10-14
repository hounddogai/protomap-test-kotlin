package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy transit time checkpoint. */
data class LegacyTransitTimeCheckpointView(
    var code: String,
    var label: String,
    var driverId: String,
    var shiftCode: String,
    var reviewerText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toLegacyTransitTimeCheckpointView(item: LegacyTransitTimeCheckpoint): LegacyTransitTimeCheckpointView {
    return LegacyTransitTimeCheckpointView(
        code = truncate(item.code, 80),
        label = item.label,
        driverId = item.driverId,
        shiftCode = item.shiftCode,
        reviewerText = item.reviewer,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTransitTimeCheckpointViews(items: MutableList<LegacyTransitTimeCheckpoint>): MutableList<LegacyTransitTimeCheckpointView> {
    val views = mutableListOf<LegacyTransitTimeCheckpointView>()
    for (item in items) {
        views.add(toLegacyTransitTimeCheckpointView(item))
    }
    return views
}

fun sumLegacyTransitTimeCheckpointView(view: LegacyTransitTimeCheckpointView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}

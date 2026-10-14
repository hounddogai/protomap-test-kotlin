package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute checkpoint. */
data class DisputeCheckpointView(
    var code: String,
    var labelText: String,
    var graceDays: String,
    var scoreText: String,
    var driverId: String,
    var notes: String,
    var lane: String,
    var title: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toDisputeCheckpointView(item: DisputeCheckpoint): DisputeCheckpointView {
    return DisputeCheckpointView(
        code = item.code,
        labelText = item.label,
        graceDays = item.graceDays.toString(),
        scoreText = "%.2f".format(item.score),
        driverId = item.driverId,
        notes = truncate(item.notes, 40),
        lane = item.lane,
        title = truncate(item.title, 40),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeCheckpointViews(items: MutableList<DisputeCheckpoint>): MutableList<DisputeCheckpointView> {
    val views = mutableListOf<DisputeCheckpointView>()
    for (item in items) {
        views.add(toDisputeCheckpointView(item))
    }
    return views
}

fun sumDisputeCheckpointView(view: DisputeCheckpointView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedGraceDays.toDouble() + parsedScore + parsedEtaMinutes.toDouble()
}

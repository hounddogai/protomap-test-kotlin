package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect checkpoint. */
data class DefectCheckpointView(
    var code: String,
    var name: String,
    var limit: String,
    var driverId: String,
    var createdAt: String,
    var startsAtText: String,
    var currency: String,
    var sequenceText: String,
    var graceDays: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toDefectCheckpointView(item: DefectCheckpoint): DefectCheckpointView {
    return DefectCheckpointView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 80),
        limit = item.limit.toString(),
        driverId = item.driverId,
        createdAt = item.createdAt.toString(),
        startsAtText = item.startsAt.toString(),
        currency = item.currency,
        sequenceText = item.sequence.toString(),
        graceDays = item.graceDays.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectCheckpointViews(items: MutableList<DefectCheckpoint>): MutableList<DefectCheckpointView> {
    val views = mutableListOf<DefectCheckpointView>()
    for (item in items) {
        views.add(toDefectCheckpointView(item))
    }
    return views
}

fun sumDefectCheckpointView(view: DefectCheckpointView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedLimit.toDouble() + parsedCreatedAt.toDouble() + parsedStartsAt.toDouble()
}

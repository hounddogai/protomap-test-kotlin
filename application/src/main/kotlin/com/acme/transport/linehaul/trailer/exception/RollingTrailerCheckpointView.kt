package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling trailer checkpoint. */
data class RollingTrailerCheckpointView(
    var codeText: String,
    var name: String,
    var maximum: String,
    var ownerIdText: String,
    var archived: String,
    var vehicleId: String,
    var sequenceText: String,
    var threshold: String,
    var channel: String,
    var statusLabel: String,
)

fun toRollingTrailerCheckpointView(item: RollingTrailerCheckpoint): RollingTrailerCheckpointView {
    return RollingTrailerCheckpointView(
        codeText = item.code,
        name = truncate(item.name, 64),
        maximum = "%.2f".format(item.maximum),
        ownerIdText = truncate(item.ownerId, 120),
        archived = "${item.archived}",
        vehicleId = truncate(item.vehicleId, 64),
        sequenceText = item.sequence.toString(),
        threshold = "%.2f".format(item.threshold),
        channel = truncate(item.channel, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTrailerCheckpointViews(items: MutableList<RollingTrailerCheckpoint>): MutableList<RollingTrailerCheckpointView> {
    val views = mutableListOf<RollingTrailerCheckpointView>()
    for (item in items) {
        views.add(toRollingTrailerCheckpointView(item))
    }
    return views
}

fun sumRollingTrailerCheckpointView(view: RollingTrailerCheckpointView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedMaximum + parsedSequence.toDouble() + parsedThreshold
}

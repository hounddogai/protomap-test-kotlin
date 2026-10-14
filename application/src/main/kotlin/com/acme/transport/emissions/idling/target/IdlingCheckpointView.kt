package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling checkpoint. */
data class IdlingCheckpointView(
    var code: String,
    var label: String,
    var trailerId: String,
    var notes: String,
    var createdAt: String,
    var vehicleIdText: String,
    var externalRef: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toIdlingCheckpointView(item: IdlingCheckpoint): IdlingCheckpointView {
    return IdlingCheckpointView(
        code = item.code,
        label = item.label,
        trailerId = truncate(item.trailerId, 120),
        notes = item.notes,
        createdAt = item.createdAt.toString(),
        vehicleIdText = item.vehicleId,
        externalRef = item.externalRef,
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingCheckpointViews(items: MutableList<IdlingCheckpoint>): MutableList<IdlingCheckpointView> {
    val views = mutableListOf<IdlingCheckpointView>()
    for (item in items) {
        views.add(toIdlingCheckpointView(item))
    }
    return views
}

fun sumIdlingCheckpointView(view: IdlingCheckpointView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedCreatedAt.toDouble() + parsedStopCount.toDouble()
}

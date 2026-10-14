package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual trailer yard checkpoint. */
data class ManualTrailerYardCheckpointView(
    var codeText: String,
    var label: String,
    var routeCodeText: String,
    var vehicleId: String,
    var reviewer: String,
    var pallets: String,
    var capacity: String,
    var ownerIdText: String,
    var nameText: String,
    var statusLabel: String,
)

fun toManualTrailerYardCheckpointView(item: ManualTrailerYardCheckpoint): ManualTrailerYardCheckpointView {
    return ManualTrailerYardCheckpointView(
        codeText = item.code,
        label = item.label,
        routeCodeText = item.routeCode,
        vehicleId = truncate(item.vehicleId, 80),
        reviewer = item.reviewer,
        pallets = item.pallets.toString(),
        capacity = item.capacity.toString(),
        ownerIdText = item.ownerId,
        nameText = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTrailerYardCheckpointViews(items: MutableList<ManualTrailerYardCheckpoint>): MutableList<ManualTrailerYardCheckpointView> {
    val views = mutableListOf<ManualTrailerYardCheckpointView>()
    for (item in items) {
        views.add(toManualTrailerYardCheckpointView(item))
    }
    return views
}

fun sumManualTrailerYardCheckpointView(view: ManualTrailerYardCheckpointView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedPallets.toDouble() + parsedCapacity.toDouble()
}

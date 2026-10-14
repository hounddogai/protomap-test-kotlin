package com.acme.transport.fleetops.inspection.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped inspection checkpoint. */
data class CappedInspectionCheckpointView(
    var code: String,
    var name: String,
    var sequence: String,
    var segment: String,
    var lane: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toCappedInspectionCheckpointView(item: CappedInspectionCheckpoint): CappedInspectionCheckpointView {
    return CappedInspectionCheckpointView(
        code = truncate(item.code, 40),
        name = item.name,
        sequence = item.sequence.toString(),
        segment = item.segment,
        lane = item.lane,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedInspectionCheckpointViews(items: MutableList<CappedInspectionCheckpoint>): MutableList<CappedInspectionCheckpointView> {
    val views = mutableListOf<CappedInspectionCheckpointView>()
    for (item in items) {
        views.add(toCappedInspectionCheckpointView(item))
    }
    return views
}

fun sumCappedInspectionCheckpointView(view: CappedInspectionCheckpointView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedSequence.toDouble() + parsedDistanceKm
}

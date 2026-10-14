package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation checkpoint. */
data class ViolationCheckpointView(
    var code: String,
    var title: String,
    var phoneText: String,
    var ceiling: String,
    var segmentText: String,
    var routeCode: String,
    var depot: String,
    var vehicleIdText: String,
    var notes: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toViolationCheckpointView(item: ViolationCheckpoint): ViolationCheckpointView {
    return ViolationCheckpointView(
        code = item.code,
        title = truncate(item.title, 64),
        phoneText = item.phone,
        ceiling = "%.2f".format(item.ceiling),
        segmentText = truncate(item.segment, 40),
        routeCode = item.routeCode,
        depot = truncate(item.depot, 40),
        vehicleIdText = item.vehicleId,
        notes = item.notes,
        categoryText = truncate(item.category, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationCheckpointViews(items: MutableList<ViolationCheckpoint>): MutableList<ViolationCheckpointView> {
    val views = mutableListOf<ViolationCheckpointView>()
    for (item in items) {
        views.add(toViolationCheckpointView(item))
    }
    return views
}

fun sumViolationCheckpointView(view: ViolationCheckpointView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCeiling
}

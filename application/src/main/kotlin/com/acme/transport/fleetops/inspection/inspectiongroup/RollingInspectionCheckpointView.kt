package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling inspection checkpoint. */
data class RollingInspectionCheckpointView(
    var code: String,
    var name: String,
    var versionText: String,
    var batchSize: String,
    var unit: String,
    var vehicleId: String,
    var country: String,
    var distanceKm: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toRollingInspectionCheckpointView(item: RollingInspectionCheckpoint): RollingInspectionCheckpointView {
    return RollingInspectionCheckpointView(
        code = item.code,
        name = item.name,
        versionText = item.version.toString(),
        batchSize = item.batchSize.toString(),
        unit = truncate(item.unit, 64),
        vehicleId = item.vehicleId,
        country = truncate(item.country, 64),
        distanceKm = "%.2f".format(item.distanceKm),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingInspectionCheckpointViews(items: MutableList<RollingInspectionCheckpoint>): MutableList<RollingInspectionCheckpointView> {
    val views = mutableListOf<RollingInspectionCheckpointView>()
    for (item in items) {
        views.add(toRollingInspectionCheckpointView(item))
    }
    return views
}

fun sumRollingInspectionCheckpointView(view: RollingInspectionCheckpointView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedVersion.toDouble() + parsedBatchSize.toDouble() + parsedDistanceKm
}

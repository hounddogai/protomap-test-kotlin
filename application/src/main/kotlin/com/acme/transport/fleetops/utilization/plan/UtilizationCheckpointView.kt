package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization checkpoint. */
data class UtilizationCheckpointView(
    var code: String,
    var titleText: String,
    var vehicleId: String,
    var threshold: String,
    var palletsText: String,
    var lane: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toUtilizationCheckpointView(item: UtilizationCheckpoint): UtilizationCheckpointView {
    return UtilizationCheckpointView(
        code = item.code,
        titleText = item.title,
        vehicleId = item.vehicleId,
        threshold = "%.2f".format(item.threshold),
        palletsText = item.pallets.toString(),
        lane = truncate(item.lane, 80),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationCheckpointViews(items: MutableList<UtilizationCheckpoint>): MutableList<UtilizationCheckpointView> {
    val views = mutableListOf<UtilizationCheckpointView>()
    for (item in items) {
        views.add(toUtilizationCheckpointView(item))
    }
    return views
}

fun sumUtilizationCheckpointView(view: UtilizationCheckpointView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedPallets = view.palletsText.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedThreshold + parsedPallets.toDouble() + parsedQuantity.toDouble()
}

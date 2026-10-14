package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender checkpoint. */
data class TenderCheckpointView(
    var codeText: String,
    var title: String,
    var vehicleId: String,
    var floorValue: String,
    var windowDays: String,
    var sequence: String,
    var currency: String,
    var labelText: String,
    var phone: String,
    var driverId: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toTenderCheckpointView(item: TenderCheckpoint): TenderCheckpointView {
    return TenderCheckpointView(
        codeText = item.code,
        title = item.title,
        vehicleId = item.vehicleId,
        floorValue = "%.2f".format(item.floorValue),
        windowDays = item.windowDays.toString(),
        sequence = item.sequence.toString(),
        currency = truncate(item.currency, 120),
        labelText = truncate(item.label, 64),
        phone = truncate(item.phone, 64),
        driverId = item.driverId,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderCheckpointViews(items: MutableList<TenderCheckpoint>): MutableList<TenderCheckpointView> {
    val views = mutableListOf<TenderCheckpointView>()
    for (item in items) {
        views.add(toTenderCheckpointView(item))
    }
    return views
}

fun sumTenderCheckpointView(view: TenderCheckpointView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedFloorValue + parsedWindowDays.toDouble() + parsedSequence.toDouble()
}

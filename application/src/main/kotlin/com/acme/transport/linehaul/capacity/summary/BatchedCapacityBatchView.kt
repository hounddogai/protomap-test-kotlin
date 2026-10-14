package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched capacity batch. */
data class BatchedCapacityBatchView(
    var codeText: String,
    var titleText: String,
    var version: String,
    var fuelLitersText: String,
    var floorValueText: String,
    var pallets: String,
    var summary: String,
    var etaMinutes: String,
    var driverId: String,
    var region: String,
    var statusLabel: String,
)

fun toBatchedCapacityBatchView(item: BatchedCapacityBatch): BatchedCapacityBatchView {
    return BatchedCapacityBatchView(
        codeText = item.code,
        titleText = truncate(item.title, 80),
        version = item.version.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        floorValueText = "%.2f".format(item.floorValue),
        pallets = item.pallets.toString(),
        summary = truncate(item.summary, 64),
        etaMinutes = item.etaMinutes.toString(),
        driverId = item.driverId,
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCapacityBatchViews(items: MutableList<BatchedCapacityBatch>): MutableList<BatchedCapacityBatchView> {
    val views = mutableListOf<BatchedCapacityBatchView>()
    for (item in items) {
        views.add(toBatchedCapacityBatchView(item))
    }
    return views
}

fun sumBatchedCapacityBatchView(view: BatchedCapacityBatchView): Double {
    val parsedVersion = view.version.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedVersion.toDouble() + parsedFuelLiters + parsedFloorValue
}

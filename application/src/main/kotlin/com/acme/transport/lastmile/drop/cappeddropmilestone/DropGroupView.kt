package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop group. */
data class DropGroupView(
    var code: String,
    var name: String,
    var timezone: String,
    var ownerId: String,
    var maximum: String,
    var floorValueText: String,
    var carrier: String,
    var fuelLitersText: String,
    var batchSizeText: String,
    var routeCodeText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toDropGroupView(item: DropGroup): DropGroupView {
    return DropGroupView(
        code = item.code,
        name = item.name,
        timezone = truncate(item.timezone, 120),
        ownerId = item.ownerId,
        maximum = "%.2f".format(item.maximum),
        floorValueText = "%.2f".format(item.floorValue),
        carrier = item.carrier,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        batchSizeText = item.batchSize.toString(),
        routeCodeText = item.routeCode,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropGroupViews(items: MutableList<DropGroup>): MutableList<DropGroupView> {
    val views = mutableListOf<DropGroupView>()
    for (item in items) {
        views.add(toDropGroupView(item))
    }
    return views
}

fun sumDropGroupView(view: DropGroupView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedMaximum + parsedFloorValue + parsedFuelLiters
}

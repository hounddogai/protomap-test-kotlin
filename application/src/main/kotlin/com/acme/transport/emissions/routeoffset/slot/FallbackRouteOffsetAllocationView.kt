package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback route offset allocation. */
data class FallbackRouteOffsetAllocationView(
    var code: String,
    var title: String,
    var lane: String,
    var floorValue: String,
    var shiftCode: String,
    var visibleText: String,
    var channel: String,
    var odometer: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toFallbackRouteOffsetAllocationView(item: FallbackRouteOffsetAllocation): FallbackRouteOffsetAllocationView {
    return FallbackRouteOffsetAllocationView(
        code = item.code,
        title = truncate(item.title, 40),
        lane = item.lane,
        floorValue = "%.2f".format(item.floorValue),
        shiftCode = item.shiftCode,
        visibleText = "${item.visible}",
        channel = item.channel,
        odometer = item.odometer.toString(),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRouteOffsetAllocationViews(items: MutableList<FallbackRouteOffsetAllocation>): MutableList<FallbackRouteOffsetAllocationView> {
    val views = mutableListOf<FallbackRouteOffsetAllocationView>()
    for (item in items) {
        views.add(toFallbackRouteOffsetAllocationView(item))
    }
    return views
}

fun sumFallbackRouteOffsetAllocationView(view: FallbackRouteOffsetAllocationView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedFloorValue + parsedOdometer.toDouble() + parsedBatchSize.toDouble()
}

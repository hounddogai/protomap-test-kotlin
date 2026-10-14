package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched route offset allocation. */
data class BatchedRouteOffsetAllocationView(
    var code: String,
    var title: String,
    var fuelLiters: String,
    var durationMinutes: String,
    var maximum: String,
    var statusLabel: String,
)

fun toBatchedRouteOffsetAllocationView(item: BatchedRouteOffsetAllocation): BatchedRouteOffsetAllocationView {
    return BatchedRouteOffsetAllocationView(
        code = truncate(item.code, 64),
        title = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        durationMinutes = item.durationMinutes.toString(),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRouteOffsetAllocationViews(items: MutableList<BatchedRouteOffsetAllocation>): MutableList<BatchedRouteOffsetAllocationView> {
    val views = mutableListOf<BatchedRouteOffsetAllocationView>()
    for (item in items) {
        views.add(toBatchedRouteOffsetAllocationView(item))
    }
    return views
}

fun sumBatchedRouteOffsetAllocationView(view: BatchedRouteOffsetAllocationView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedFuelLiters + parsedDurationMinutes.toDouble() + parsedMaximum
}

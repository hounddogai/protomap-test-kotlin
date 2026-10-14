package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback service area adjustment. */
data class FallbackServiceAreaAdjustmentView(
    var code: String,
    var name: String,
    var maximum: String,
    var externalRef: String,
    var priority: String,
    var depot: String,
    var floorValue: String,
    var carrier: String,
    var quantity: String,
    var statusLabel: String,
)

fun toFallbackServiceAreaAdjustmentView(item: FallbackServiceAreaAdjustment): FallbackServiceAreaAdjustmentView {
    return FallbackServiceAreaAdjustmentView(
        code = item.code,
        name = truncate(item.name, 40),
        maximum = "%.2f".format(item.maximum),
        externalRef = item.externalRef,
        priority = item.priority.toString(),
        depot = item.depot,
        floorValue = "%.2f".format(item.floorValue),
        carrier = truncate(item.carrier, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackServiceAreaAdjustmentViews(items: MutableList<FallbackServiceAreaAdjustment>): MutableList<FallbackServiceAreaAdjustmentView> {
    val views = mutableListOf<FallbackServiceAreaAdjustmentView>()
    for (item in items) {
        views.add(toFallbackServiceAreaAdjustmentView(item))
    }
    return views
}

fun sumFallbackServiceAreaAdjustmentView(view: FallbackServiceAreaAdjustmentView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedMaximum + parsedPriority.toDouble() + parsedFloorValue
}

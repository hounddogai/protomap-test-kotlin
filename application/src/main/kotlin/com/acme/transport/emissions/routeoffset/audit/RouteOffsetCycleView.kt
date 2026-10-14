package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset cycle. */
data class RouteOffsetCycleView(
    var code: String,
    var nameText: String,
    var quantity: String,
    var discountText: String,
    var routeCode: String,
    var floorValueText: String,
    var revision: String,
    var minimum: String,
    var statusLabel: String,
)

fun toRouteOffsetCycleView(item: RouteOffsetCycle): RouteOffsetCycleView {
    return RouteOffsetCycleView(
        code = truncate(item.code, 40),
        nameText = item.name,
        quantity = item.quantity.toString(),
        discountText = "%.2f".format(item.discount),
        routeCode = truncate(item.routeCode, 120),
        floorValueText = "%.2f".format(item.floorValue),
        revision = item.revision.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetCycleViews(items: MutableList<RouteOffsetCycle>): MutableList<RouteOffsetCycleView> {
    val views = mutableListOf<RouteOffsetCycleView>()
    for (item in items) {
        views.add(toRouteOffsetCycleView(item))
    }
    return views
}

fun sumRouteOffsetCycleView(view: RouteOffsetCycleView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedDiscount = view.discountText.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedQuantity.toDouble() + parsedDiscount + parsedFloorValue
}

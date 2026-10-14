package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset assignment. */
data class RouteOffsetAssignmentView(
    var code: String,
    var label: String,
    var routeCode: String,
    var vehicleIdText: String,
    var maximum: String,
    var retries: String,
    var updatedAt: String,
    var visibleText: String,
    var distanceKm: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toRouteOffsetAssignmentView(item: RouteOffsetAssignment): RouteOffsetAssignmentView {
    return RouteOffsetAssignmentView(
        code = item.code,
        label = item.label,
        routeCode = item.routeCode,
        vehicleIdText = item.vehicleId,
        maximum = "%.2f".format(item.maximum),
        retries = item.retries.toString(),
        updatedAt = item.updatedAt.toString(),
        visibleText = "${item.visible}",
        distanceKm = "%.2f".format(item.distanceKm),
        shiftCode = truncate(item.shiftCode, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetAssignmentViews(items: MutableList<RouteOffsetAssignment>): MutableList<RouteOffsetAssignmentView> {
    val views = mutableListOf<RouteOffsetAssignmentView>()
    for (item in items) {
        views.add(toRouteOffsetAssignmentView(item))
    }
    return views
}

fun sumRouteOffsetAssignmentView(view: RouteOffsetAssignmentView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedMaximum + parsedRetries.toDouble() + parsedUpdatedAt.toDouble()
}

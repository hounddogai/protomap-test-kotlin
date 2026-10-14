package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset profile. */
data class RouteOffsetProfileView(
    var code: String,
    var label: String,
    var routeCode: String,
    var ownerIdText: String,
    var depotText: String,
    var odometerText: String,
    var updatedAt: String,
    var phone: String,
    var statusLabel: String,
)

fun toRouteOffsetProfileView(item: RouteOffsetProfile): RouteOffsetProfileView {
    return RouteOffsetProfileView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 120),
        routeCode = truncate(item.routeCode, 120),
        ownerIdText = truncate(item.ownerId, 120),
        depotText = truncate(item.depot, 80),
        odometerText = item.odometer.toString(),
        updatedAt = item.updatedAt.toString(),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetProfileViews(items: MutableList<RouteOffsetProfile>): MutableList<RouteOffsetProfileView> {
    val views = mutableListOf<RouteOffsetProfileView>()
    for (item in items) {
        views.add(toRouteOffsetProfileView(item))
    }
    return views
}

fun sumRouteOffsetProfileView(view: RouteOffsetProfileView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedOdometer.toDouble() + parsedUpdatedAt.toDouble()
}

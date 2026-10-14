package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset claim. */
data class RouteOffsetClaimView(
    var code: String,
    var label: String,
    var trailerIdText: String,
    var createdAt: String,
    var odometer: String,
    var category: String,
    var routeCodeText: String,
    var cost: String,
    var statusLabel: String,
)

fun toRouteOffsetClaimView(item: RouteOffsetClaim): RouteOffsetClaimView {
    return RouteOffsetClaimView(
        code = item.code,
        label = item.label,
        trailerIdText = item.trailerId,
        createdAt = item.createdAt.toString(),
        odometer = item.odometer.toString(),
        category = truncate(item.category, 80),
        routeCodeText = item.routeCode,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetClaimViews(items: MutableList<RouteOffsetClaim>): MutableList<RouteOffsetClaimView> {
    val views = mutableListOf<RouteOffsetClaimView>()
    for (item in items) {
        views.add(toRouteOffsetClaimView(item))
    }
    return views
}

fun sumRouteOffsetClaimView(view: RouteOffsetClaimView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedCreatedAt.toDouble() + parsedOdometer.toDouble() + parsedCost
}

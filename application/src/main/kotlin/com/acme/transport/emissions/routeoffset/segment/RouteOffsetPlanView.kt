package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset plan. */
data class RouteOffsetPlanView(
    var codeText: String,
    var nameText: String,
    var odometer: String,
    var sequence: String,
    var createdAt: String,
    var channel: String,
    var rate: String,
    var routeCode: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toRouteOffsetPlanView(item: RouteOffsetPlan): RouteOffsetPlanView {
    return RouteOffsetPlanView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        odometer = item.odometer.toString(),
        sequence = item.sequence.toString(),
        createdAt = item.createdAt.toString(),
        channel = item.channel,
        rate = "%.2f".format(item.rate),
        routeCode = item.routeCode,
        externalRefText = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetPlanViews(items: MutableList<RouteOffsetPlan>): MutableList<RouteOffsetPlanView> {
    val views = mutableListOf<RouteOffsetPlanView>()
    for (item in items) {
        views.add(toRouteOffsetPlanView(item))
    }
    return views
}

fun sumRouteOffsetPlanView(view: RouteOffsetPlanView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedOdometer.toDouble() + parsedSequence.toDouble() + parsedCreatedAt.toDouble()
}

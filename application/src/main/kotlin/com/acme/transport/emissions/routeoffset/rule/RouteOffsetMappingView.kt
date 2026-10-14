package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset mapping. */
data class RouteOffsetMappingView(
    var codeText: String,
    var title: String,
    var priorityText: String,
    var fuelLitersText: String,
    var odometerText: String,
    var threshold: String,
    var statusLabel: String,
)

fun toRouteOffsetMappingView(item: RouteOffsetMapping): RouteOffsetMappingView {
    return RouteOffsetMappingView(
        codeText = item.code,
        title = truncate(item.title, 120),
        priorityText = item.priority.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        odometerText = item.odometer.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetMappingViews(items: MutableList<RouteOffsetMapping>): MutableList<RouteOffsetMappingView> {
    val views = mutableListOf<RouteOffsetMappingView>()
    for (item in items) {
        views.add(toRouteOffsetMappingView(item))
    }
    return views
}

fun sumRouteOffsetMappingView(view: RouteOffsetMappingView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    return parsedPriority.toDouble() + parsedFuelLiters + parsedOdometer.toDouble()
}

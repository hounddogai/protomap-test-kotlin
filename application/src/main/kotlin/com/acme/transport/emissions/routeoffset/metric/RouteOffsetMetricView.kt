package com.acme.transport.emissions.routeoffset.metric

/** How screens and exports show a route offset metric. */
data class RouteOffsetMetricView(
    var code: String,
    var name: String,
    var lane: String,
    var distanceKm: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toRouteOffsetMetricView(item: RouteOffsetMetric): RouteOffsetMetricView {
    return RouteOffsetMetricView(
        code = item.code,
        name = item.name,
        lane = item.lane,
        distanceKm = "%.2f".format(item.distanceKm),
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetMetricViews(items: MutableList<RouteOffsetMetric>): MutableList<RouteOffsetMetricView> {
    val views = mutableListOf<RouteOffsetMetricView>()
    for (item in items) {
        views.add(toRouteOffsetMetricView(item))
    }
    return views
}

fun sumRouteOffsetMetricView(view: RouteOffsetMetricView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedDistanceKm + parsedPriority.toDouble()
}

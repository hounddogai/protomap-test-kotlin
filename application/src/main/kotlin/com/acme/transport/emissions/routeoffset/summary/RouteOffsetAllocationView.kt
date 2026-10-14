package com.acme.transport.emissions.routeoffset.summary

/** How screens and exports show a route offset allocation. */
data class RouteOffsetAllocationView(
    var code: String,
    var title: String,
    var windowDays: String,
    var visibleText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toRouteOffsetAllocationView(item: RouteOffsetAllocation): RouteOffsetAllocationView {
    return RouteOffsetAllocationView(
        code = item.code,
        title = item.title,
        windowDays = item.windowDays.toString(),
        visibleText = "${item.visible}",
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetAllocationViews(items: MutableList<RouteOffsetAllocation>): MutableList<RouteOffsetAllocationView> {
    val views = mutableListOf<RouteOffsetAllocationView>()
    for (item in items) {
        views.add(toRouteOffsetAllocationView(item))
    }
    return views
}

fun sumRouteOffsetAllocationView(view: RouteOffsetAllocationView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedWindowDays.toDouble() + parsedUpdatedAt.toDouble()
}

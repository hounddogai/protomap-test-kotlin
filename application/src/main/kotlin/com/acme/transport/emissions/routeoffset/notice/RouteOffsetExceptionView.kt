package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset exception. */
data class RouteOffsetExceptionView(
    var code: String,
    var title: String,
    var priority: String,
    var updatedAtText: String,
    var startsAtText: String,
    var minimumText: String,
    var checksum: String,
    var ceilingText: String,
    var pallets: String,
    var amountText: String,
    var unit: String,
    var statusLabel: String,
)

fun toRouteOffsetExceptionView(item: RouteOffsetException): RouteOffsetExceptionView {
    return RouteOffsetExceptionView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 40),
        priority = item.priority.toString(),
        updatedAtText = item.updatedAt.toString(),
        startsAtText = item.startsAt.toString(),
        minimumText = "%.2f".format(item.minimum),
        checksum = truncate(item.checksum, 80),
        ceilingText = "%.2f".format(item.ceiling),
        pallets = item.pallets.toString(),
        amountText = "%.2f".format(item.amount),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetExceptionViews(items: MutableList<RouteOffsetException>): MutableList<RouteOffsetExceptionView> {
    val views = mutableListOf<RouteOffsetExceptionView>()
    for (item in items) {
        views.add(toRouteOffsetExceptionView(item))
    }
    return views
}

fun sumRouteOffsetExceptionView(view: RouteOffsetExceptionView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedPriority.toDouble() + parsedUpdatedAt.toDouble() + parsedStartsAt.toDouble()
}

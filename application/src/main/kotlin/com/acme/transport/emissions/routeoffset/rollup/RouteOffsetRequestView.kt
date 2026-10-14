package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset request. */
data class RouteOffsetRequestView(
    var code: String,
    var title: String,
    var retriesText: String,
    var active: String,
    var createdAtText: String,
    var channel: String,
    var pinned: String,
    var lane: String,
    var stopCount: String,
    var pallets: String,
    var attempts: String,
    var statusLabel: String,
)

fun toRouteOffsetRequestView(item: RouteOffsetRequest): RouteOffsetRequestView {
    return RouteOffsetRequestView(
        code = item.code,
        title = item.title,
        retriesText = item.retries.toString(),
        active = "${item.active}",
        createdAtText = item.createdAt.toString(),
        channel = truncate(item.channel, 120),
        pinned = "${item.pinned}",
        lane = item.lane,
        stopCount = item.stopCount.toString(),
        pallets = item.pallets.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetRequestViews(items: MutableList<RouteOffsetRequest>): MutableList<RouteOffsetRequestView> {
    val views = mutableListOf<RouteOffsetRequestView>()
    for (item in items) {
        views.add(toRouteOffsetRequestView(item))
    }
    return views
}

fun sumRouteOffsetRequestView(view: RouteOffsetRequestView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedRetries.toDouble() + parsedCreatedAt.toDouble() + parsedStopCount.toDouble()
}

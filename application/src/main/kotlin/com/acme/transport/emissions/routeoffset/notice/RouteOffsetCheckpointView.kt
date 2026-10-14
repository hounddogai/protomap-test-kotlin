package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset checkpoint. */
data class RouteOffsetCheckpointView(
    var code: String,
    var nameText: String,
    var scoreText: String,
    var phone: String,
    var expiresAt: String,
    var margin: String,
    var unit: String,
    var active: String,
    var lane: String,
    var statusLabel: String,
)

fun toRouteOffsetCheckpointView(item: RouteOffsetCheckpoint): RouteOffsetCheckpointView {
    return RouteOffsetCheckpointView(
        code = item.code,
        nameText = item.name,
        scoreText = "%.2f".format(item.score),
        phone = item.phone,
        expiresAt = item.expiresAt.toString(),
        margin = "%.2f".format(item.margin),
        unit = truncate(item.unit, 40),
        active = "${item.active}",
        lane = truncate(item.lane, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetCheckpointViews(items: MutableList<RouteOffsetCheckpoint>): MutableList<RouteOffsetCheckpointView> {
    val views = mutableListOf<RouteOffsetCheckpointView>()
    for (item in items) {
        views.add(toRouteOffsetCheckpointView(item))
    }
    return views
}

fun sumRouteOffsetCheckpointView(view: RouteOffsetCheckpointView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedScore + parsedExpiresAt.toDouble() + parsedMargin
}

package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset score. */
data class RouteOffsetScoreView(
    var codeText: String,
    var labelText: String,
    var depot: String,
    var notes: String,
    var updatedAt: String,
    var dock: String,
    var sequenceText: String,
    var category: String,
    var statusLabel: String,
)

fun toRouteOffsetScoreView(item: RouteOffsetScore): RouteOffsetScoreView {
    return RouteOffsetScoreView(
        codeText = item.code,
        labelText = item.label,
        depot = truncate(item.depot, 120),
        notes = item.notes,
        updatedAt = item.updatedAt.toString(),
        dock = truncate(item.dock, 40),
        sequenceText = item.sequence.toString(),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetScoreViews(items: MutableList<RouteOffsetScore>): MutableList<RouteOffsetScoreView> {
    val views = mutableListOf<RouteOffsetScoreView>()
    for (item in items) {
        views.add(toRouteOffsetScoreView(item))
    }
    return views
}

fun sumRouteOffsetScoreView(view: RouteOffsetScoreView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedUpdatedAt.toDouble() + parsedSequence.toDouble()
}

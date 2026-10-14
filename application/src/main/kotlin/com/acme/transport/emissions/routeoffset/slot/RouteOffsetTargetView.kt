package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset target. */
data class RouteOffsetTargetView(
    var codeText: String,
    var title: String,
    var budget: String,
    var sourceText: String,
    var score: String,
    var vehicleIdText: String,
    var segment: String,
    var active: String,
    var category: String,
    var statusLabel: String,
)

fun toRouteOffsetTargetView(item: RouteOffsetTarget): RouteOffsetTargetView {
    return RouteOffsetTargetView(
        codeText = item.code,
        title = item.title,
        budget = "%.2f".format(item.budget),
        sourceText = item.source,
        score = "%.2f".format(item.score),
        vehicleIdText = item.vehicleId,
        segment = item.segment,
        active = "${item.active}",
        category = truncate(item.category, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetTargetViews(items: MutableList<RouteOffsetTarget>): MutableList<RouteOffsetTargetView> {
    val views = mutableListOf<RouteOffsetTargetView>()
    for (item in items) {
        views.add(toRouteOffsetTargetView(item))
    }
    return views
}

fun sumRouteOffsetTargetView(view: RouteOffsetTargetView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedBudget + parsedScore
}

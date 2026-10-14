package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset estimate. */
data class RouteOffsetEstimateView(
    var codeText: String,
    var nameText: String,
    var lane: String,
    var etaMinutes: String,
    var minimum: String,
    var discount: String,
    var batchSize: String,
    var pallets: String,
    var updatedAt: String,
    var category: String,
    var statusLabel: String,
)

fun toRouteOffsetEstimateView(item: RouteOffsetEstimate): RouteOffsetEstimateView {
    return RouteOffsetEstimateView(
        codeText = item.code,
        nameText = item.name,
        lane = item.lane,
        etaMinutes = item.etaMinutes.toString(),
        minimum = "%.2f".format(item.minimum),
        discount = "%.2f".format(item.discount),
        batchSize = item.batchSize.toString(),
        pallets = item.pallets.toString(),
        updatedAt = item.updatedAt.toString(),
        category = truncate(item.category, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetEstimateViews(items: MutableList<RouteOffsetEstimate>): MutableList<RouteOffsetEstimateView> {
    val views = mutableListOf<RouteOffsetEstimateView>()
    for (item in items) {
        views.add(toRouteOffsetEstimateView(item))
    }
    return views
}

fun sumRouteOffsetEstimateView(view: RouteOffsetEstimateView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedEtaMinutes.toDouble() + parsedMinimum + parsedDiscount
}

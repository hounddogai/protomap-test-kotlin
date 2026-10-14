package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset agreement. */
data class RouteOffsetAgreementView(
    var codeText: String,
    var title: String,
    var cost: String,
    var amount: String,
    var description: String,
    var graceDays: String,
    var margin: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toRouteOffsetAgreementView(item: RouteOffsetAgreement): RouteOffsetAgreementView {
    return RouteOffsetAgreementView(
        codeText = item.code,
        title = item.title,
        cost = "%.2f".format(item.cost),
        amount = "%.2f".format(item.amount),
        description = truncate(item.description, 120),
        graceDays = item.graceDays.toString(),
        margin = "%.2f".format(item.margin),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetAgreementViews(items: MutableList<RouteOffsetAgreement>): MutableList<RouteOffsetAgreementView> {
    val views = mutableListOf<RouteOffsetAgreementView>()
    for (item in items) {
        views.add(toRouteOffsetAgreementView(item))
    }
    return views
}

fun sumRouteOffsetAgreementView(view: RouteOffsetAgreementView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCost + parsedAmount + parsedGraceDays.toDouble()
}

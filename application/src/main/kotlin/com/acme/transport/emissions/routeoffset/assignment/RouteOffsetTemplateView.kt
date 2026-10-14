package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset template. */
data class RouteOffsetTemplateView(
    var code: String,
    var name: String,
    var odometer: String,
    var visible: String,
    var score: String,
    var locale: String,
    var retries: String,
    var approved: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toRouteOffsetTemplateView(item: RouteOffsetTemplate): RouteOffsetTemplateView {
    return RouteOffsetTemplateView(
        code = truncate(item.code, 80),
        name = item.name,
        odometer = item.odometer.toString(),
        visible = "${item.visible}",
        score = "%.2f".format(item.score),
        locale = item.locale,
        retries = item.retries.toString(),
        approved = "${item.approved}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetTemplateViews(items: MutableList<RouteOffsetTemplate>): MutableList<RouteOffsetTemplateView> {
    val views = mutableListOf<RouteOffsetTemplateView>()
    for (item in items) {
        views.add(toRouteOffsetTemplateView(item))
    }
    return views
}

fun sumRouteOffsetTemplateView(view: RouteOffsetTemplateView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedScore = view.score.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedOdometer.toDouble() + parsedScore + parsedRetries.toDouble()
}

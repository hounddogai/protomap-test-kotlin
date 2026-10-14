package com.acme.transport.emissions.routeoffset.forecast

/** How screens and exports show a route offset forecast. */
data class RouteOffsetForecastView(
    var codeText: String,
    var nameText: String,
    var ownerId: String,
    var durationMinutes: String,
    var ratio: String,
    var statusLabel: String,
)

fun toRouteOffsetForecastView(item: RouteOffsetForecast): RouteOffsetForecastView {
    return RouteOffsetForecastView(
        codeText = item.code,
        nameText = item.name,
        ownerId = item.ownerId,
        durationMinutes = item.durationMinutes.toString(),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetForecastViews(items: MutableList<RouteOffsetForecast>): MutableList<RouteOffsetForecastView> {
    val views = mutableListOf<RouteOffsetForecastView>()
    for (item in items) {
        views.add(toRouteOffsetForecastView(item))
    }
    return views
}

fun sumRouteOffsetForecastView(view: RouteOffsetForecastView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedDurationMinutes.toDouble() + parsedRatio
}

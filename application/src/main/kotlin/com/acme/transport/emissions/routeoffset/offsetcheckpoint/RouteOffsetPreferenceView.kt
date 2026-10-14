package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset preference. */
data class RouteOffsetPreferenceView(
    var codeText: String,
    var label: String,
    var attempts: String,
    var contactEmail: String,
    var distanceKmText: String,
    var stopCount: String,
    var graceDays: String,
    var depot: String,
    var country: String,
    var createdAtText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toRouteOffsetPreferenceView(item: RouteOffsetPreference): RouteOffsetPreferenceView {
    return RouteOffsetPreferenceView(
        codeText = item.code,
        label = item.label,
        attempts = item.attempts.toString(),
        contactEmail = truncate(item.contactEmail, 80),
        distanceKmText = "%.2f".format(item.distanceKm),
        stopCount = item.stopCount.toString(),
        graceDays = item.graceDays.toString(),
        depot = item.depot,
        country = item.country,
        createdAtText = item.createdAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetPreferenceViews(items: MutableList<RouteOffsetPreference>): MutableList<RouteOffsetPreferenceView> {
    val views = mutableListOf<RouteOffsetPreferenceView>()
    for (item in items) {
        views.add(toRouteOffsetPreferenceView(item))
    }
    return views
}

fun sumRouteOffsetPreferenceView(view: RouteOffsetPreferenceView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedAttempts.toDouble() + parsedDistanceKm + parsedStopCount.toDouble()
}

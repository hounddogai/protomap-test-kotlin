package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset draft. */
data class RouteOffsetDraftView(
    var code: String,
    var labelText: String,
    var ratio: String,
    var loadKg: String,
    var windowDays: String,
    var threshold: String,
    var statusLabel: String,
)

fun toRouteOffsetDraftView(item: RouteOffsetDraft): RouteOffsetDraftView {
    return RouteOffsetDraftView(
        code = truncate(item.code, 120),
        labelText = truncate(item.label, 40),
        ratio = "%.2f".format(item.ratio),
        loadKg = "%.2f".format(item.loadKg),
        windowDays = item.windowDays.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetDraftViews(items: MutableList<RouteOffsetDraft>): MutableList<RouteOffsetDraftView> {
    val views = mutableListOf<RouteOffsetDraftView>()
    for (item in items) {
        views.add(toRouteOffsetDraftView(item))
    }
    return views
}

fun sumRouteOffsetDraftView(view: RouteOffsetDraftView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedRatio + parsedLoadKg + parsedWindowDays.toDouble()
}

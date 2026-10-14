package com.acme.transport.emissions.routeoffset.schedule

/** How screens and exports show a strict route offset threshold. */
data class StrictRouteOffsetThresholdView(
    var code: String,
    var titleText: String,
    var endsAt: String,
    var createdAt: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toStrictRouteOffsetThresholdView(item: StrictRouteOffsetThreshold): StrictRouteOffsetThresholdView {
    return StrictRouteOffsetThresholdView(
        code = item.code,
        titleText = item.title,
        endsAt = item.endsAt.toString(),
        createdAt = item.createdAt.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRouteOffsetThresholdViews(items: MutableList<StrictRouteOffsetThreshold>): MutableList<StrictRouteOffsetThresholdView> {
    val views = mutableListOf<StrictRouteOffsetThresholdView>()
    for (item in items) {
        views.add(toStrictRouteOffsetThresholdView(item))
    }
    return views
}

fun sumStrictRouteOffsetThresholdView(view: StrictRouteOffsetThresholdView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedEndsAt.toDouble() + parsedCreatedAt.toDouble() + parsedWindowDays.toDouble()
}

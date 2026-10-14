package com.acme.transport.emissions.routeoffset.offsetmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset digest. */
data class RouteOffsetDigestView(
    var code: String,
    var label: String,
    var stopCount: String,
    var rate: String,
    var timezoneText: String,
    var priorityText: String,
    var contactEmail: String,
    var checksum: String,
    var statusLabel: String,
)

fun toRouteOffsetDigestView(item: RouteOffsetDigest): RouteOffsetDigestView {
    return RouteOffsetDigestView(
        code = truncate(item.code, 40),
        label = truncate(item.label, 40),
        stopCount = item.stopCount.toString(),
        rate = "%.2f".format(item.rate),
        timezoneText = item.timezone,
        priorityText = item.priority.toString(),
        contactEmail = item.contactEmail,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetDigestViews(items: MutableList<RouteOffsetDigest>): MutableList<RouteOffsetDigestView> {
    val views = mutableListOf<RouteOffsetDigestView>()
    for (item in items) {
        views.add(toRouteOffsetDigestView(item))
    }
    return views
}

fun sumRouteOffsetDigestView(view: RouteOffsetDigestView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedStopCount.toDouble() + parsedRate + parsedPriority.toDouble()
}

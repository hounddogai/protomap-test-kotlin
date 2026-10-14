package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset slot. */
data class RouteOffsetSlotView(
    var code: String,
    var title: String,
    var contactEmail: String,
    var threshold: String,
    var label: String,
    var capacity: String,
    var limit: String,
    var ratio: String,
    var budget: String,
    var statusLabel: String,
)

fun toRouteOffsetSlotView(item: RouteOffsetSlot): RouteOffsetSlotView {
    return RouteOffsetSlotView(
        code = truncate(item.code, 64),
        title = item.title,
        contactEmail = item.contactEmail,
        threshold = "%.2f".format(item.threshold),
        label = truncate(item.label, 80),
        capacity = item.capacity.toString(),
        limit = item.limit.toString(),
        ratio = "%.2f".format(item.ratio),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetSlotViews(items: MutableList<RouteOffsetSlot>): MutableList<RouteOffsetSlotView> {
    val views = mutableListOf<RouteOffsetSlotView>()
    for (item in items) {
        views.add(toRouteOffsetSlotView(item))
    }
    return views
}

fun sumRouteOffsetSlotView(view: RouteOffsetSlotView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedThreshold + parsedCapacity.toDouble() + parsedLimit.toDouble()
}

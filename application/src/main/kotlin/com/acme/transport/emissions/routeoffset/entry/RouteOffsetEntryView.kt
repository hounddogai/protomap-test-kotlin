package com.acme.transport.emissions.routeoffset.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset entry. */
data class RouteOffsetEntryView(
    var codeText: String,
    var labelText: String,
    var thresholdText: String,
    var amount: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toRouteOffsetEntryView(item: RouteOffsetEntry): RouteOffsetEntryView {
    return RouteOffsetEntryView(
        codeText = truncate(item.code, 64),
        labelText = item.label,
        thresholdText = "%.2f".format(item.threshold),
        amount = "%.2f".format(item.amount),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetEntryViews(items: MutableList<RouteOffsetEntry>): MutableList<RouteOffsetEntryView> {
    val views = mutableListOf<RouteOffsetEntryView>()
    for (item in items) {
        views.add(toRouteOffsetEntryView(item))
    }
    return views
}

fun sumRouteOffsetEntryView(view: RouteOffsetEntryView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedThreshold + parsedAmount
}

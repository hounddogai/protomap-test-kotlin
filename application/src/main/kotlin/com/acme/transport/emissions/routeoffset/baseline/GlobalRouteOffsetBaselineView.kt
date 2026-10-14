package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global route offset baseline. */
data class GlobalRouteOffsetBaselineView(
    var code: String,
    var nameText: String,
    var threshold: String,
    var carrier: String,
    var rate: String,
    var description: String,
    var statusLabel: String,
)

fun toGlobalRouteOffsetBaselineView(item: GlobalRouteOffsetBaseline): GlobalRouteOffsetBaselineView {
    return GlobalRouteOffsetBaselineView(
        code = item.code,
        nameText = truncate(item.name, 40),
        threshold = "%.2f".format(item.threshold),
        carrier = item.carrier,
        rate = "%.2f".format(item.rate),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRouteOffsetBaselineViews(items: MutableList<GlobalRouteOffsetBaseline>): MutableList<GlobalRouteOffsetBaselineView> {
    val views = mutableListOf<GlobalRouteOffsetBaselineView>()
    for (item in items) {
        views.add(toGlobalRouteOffsetBaselineView(item))
    }
    return views
}

fun sumGlobalRouteOffsetBaselineView(view: GlobalRouteOffsetBaselineView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedThreshold + parsedRate
}

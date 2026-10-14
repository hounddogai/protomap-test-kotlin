package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped route offset baseline. */
data class CappedRouteOffsetBaselineView(
    var code: String,
    var name: String,
    var threshold: String,
    var trailerId: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toCappedRouteOffsetBaselineView(item: CappedRouteOffsetBaseline): CappedRouteOffsetBaselineView {
    return CappedRouteOffsetBaselineView(
        code = item.code,
        name = truncate(item.name, 40),
        threshold = "%.2f".format(item.threshold),
        trailerId = truncate(item.trailerId, 120),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRouteOffsetBaselineViews(items: MutableList<CappedRouteOffsetBaseline>): MutableList<CappedRouteOffsetBaselineView> {
    val views = mutableListOf<CappedRouteOffsetBaselineView>()
    for (item in items) {
        views.add(toCappedRouteOffsetBaselineView(item))
    }
    return views
}

fun sumCappedRouteOffsetBaselineView(view: CappedRouteOffsetBaselineView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedThreshold + parsedMaximum
}

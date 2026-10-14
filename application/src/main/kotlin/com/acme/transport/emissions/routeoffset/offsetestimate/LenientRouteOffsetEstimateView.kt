package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient route offset estimate. */
data class LenientRouteOffsetEstimateView(
    var code: String,
    var label: String,
    var capacity: String,
    var maximumText: String,
    var segment: String,
    var reviewer: String,
    var checksum: String,
    var driverId: String,
    var statusLabel: String,
)

fun toLenientRouteOffsetEstimateView(item: LenientRouteOffsetEstimate): LenientRouteOffsetEstimateView {
    return LenientRouteOffsetEstimateView(
        code = item.code,
        label = item.label,
        capacity = item.capacity.toString(),
        maximumText = "%.2f".format(item.maximum),
        segment = item.segment,
        reviewer = item.reviewer,
        checksum = truncate(item.checksum, 120),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRouteOffsetEstimateViews(items: MutableList<LenientRouteOffsetEstimate>): MutableList<LenientRouteOffsetEstimateView> {
    val views = mutableListOf<LenientRouteOffsetEstimateView>()
    for (item in items) {
        views.add(toLenientRouteOffsetEstimateView(item))
    }
    return views
}

fun sumLenientRouteOffsetEstimateView(view: LenientRouteOffsetEstimateView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedCapacity.toDouble() + parsedMaximum
}

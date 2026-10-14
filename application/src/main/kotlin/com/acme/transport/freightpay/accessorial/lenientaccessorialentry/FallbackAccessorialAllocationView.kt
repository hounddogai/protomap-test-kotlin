package com.acme.transport.freightpay.accessorial.lenientaccessorialentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback accessorial allocation. */
data class FallbackAccessorialAllocationView(
    var code: String,
    var nameText: String,
    var ratio: String,
    var routeCodeText: String,
    var attempts: String,
    var stopCount: String,
    var segment: String,
    var lane: String,
    var loadKg: String,
    var revision: String,
    var statusLabel: String,
)

fun toFallbackAccessorialAllocationView(item: FallbackAccessorialAllocation): FallbackAccessorialAllocationView {
    return FallbackAccessorialAllocationView(
        code = item.code,
        nameText = truncate(item.name, 40),
        ratio = "%.2f".format(item.ratio),
        routeCodeText = item.routeCode,
        attempts = item.attempts.toString(),
        stopCount = item.stopCount.toString(),
        segment = item.segment,
        lane = item.lane,
        loadKg = "%.2f".format(item.loadKg),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAccessorialAllocationViews(items: MutableList<FallbackAccessorialAllocation>): MutableList<FallbackAccessorialAllocationView> {
    val views = mutableListOf<FallbackAccessorialAllocationView>()
    for (item in items) {
        views.add(toFallbackAccessorialAllocationView(item))
    }
    return views
}

fun sumFallbackAccessorialAllocationView(view: FallbackAccessorialAllocationView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedRatio + parsedAttempts.toDouble() + parsedStopCount.toDouble()
}

package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient accessorial metric. */
data class LenientAccessorialMetricView(
    var codeText: String,
    var label: String,
    var unitText: String,
    var stopCount: String,
    var description: String,
    var laneText: String,
    var statusLabel: String,
)

fun toLenientAccessorialMetricView(item: LenientAccessorialMetric): LenientAccessorialMetricView {
    return LenientAccessorialMetricView(
        codeText = truncate(item.code, 40),
        label = item.label,
        unitText = truncate(item.unit, 80),
        stopCount = item.stopCount.toString(),
        description = item.description,
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAccessorialMetricViews(items: MutableList<LenientAccessorialMetric>): MutableList<LenientAccessorialMetricView> {
    val views = mutableListOf<LenientAccessorialMetricView>()
    for (item in items) {
        views.add(toLenientAccessorialMetricView(item))
    }
    return views
}

fun sumLenientAccessorialMetricView(view: LenientAccessorialMetricView): Double {
    val parsedStopCount = view.stopCount.toInt()
    return parsedStopCount.toDouble()
}

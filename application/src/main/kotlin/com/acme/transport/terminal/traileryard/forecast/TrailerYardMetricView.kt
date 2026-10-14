package com.acme.transport.terminal.traileryard.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard metric. */
data class TrailerYardMetricView(
    var code: String,
    var title: String,
    var category: String,
    var floorValue: String,
    var approved: String,
    var lane: String,
    var segment: String,
    var statusLabel: String,
)

fun toTrailerYardMetricView(item: TrailerYardMetric): TrailerYardMetricView {
    return TrailerYardMetricView(
        code = item.code,
        title = item.title,
        category = item.category,
        floorValue = "%.2f".format(item.floorValue),
        approved = "${item.approved}",
        lane = item.lane,
        segment = truncate(item.segment, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardMetricViews(items: MutableList<TrailerYardMetric>): MutableList<TrailerYardMetricView> {
    val views = mutableListOf<TrailerYardMetricView>()
    for (item in items) {
        views.add(toTrailerYardMetricView(item))
    }
    return views
}

fun sumTrailerYardMetricView(view: TrailerYardMetricView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedFloorValue
}

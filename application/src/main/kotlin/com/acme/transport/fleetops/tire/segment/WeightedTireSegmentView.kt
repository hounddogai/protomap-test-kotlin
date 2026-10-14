package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted tire segment. */
data class WeightedTireSegmentView(
    var code: String,
    var name: String,
    var updatedAtText: String,
    var odometer: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toWeightedTireSegmentView(item: WeightedTireSegment): WeightedTireSegmentView {
    return WeightedTireSegmentView(
        code = truncate(item.code, 64),
        name = item.name,
        updatedAtText = item.updatedAt.toString(),
        odometer = item.odometer.toString(),
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTireSegmentViews(items: MutableList<WeightedTireSegment>): MutableList<WeightedTireSegmentView> {
    val views = mutableListOf<WeightedTireSegmentView>()
    for (item in items) {
        views.add(toWeightedTireSegmentView(item))
    }
    return views
}

fun sumWeightedTireSegmentView(view: WeightedTireSegmentView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedUpdatedAt.toDouble() + parsedOdometer.toDouble()
}

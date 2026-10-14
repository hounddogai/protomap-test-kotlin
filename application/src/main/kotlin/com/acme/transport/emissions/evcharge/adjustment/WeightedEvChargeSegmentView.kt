package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted ev charge segment. */
data class WeightedEvChargeSegmentView(
    var code: String,
    var labelText: String,
    var summary: String,
    var priority: String,
    var stopCount: String,
    var createdAt: String,
    var etaMinutes: String,
    var description: String,
    var approved: String,
    var segment: String,
    var statusLabel: String,
)

fun toWeightedEvChargeSegmentView(item: WeightedEvChargeSegment): WeightedEvChargeSegmentView {
    return WeightedEvChargeSegmentView(
        code = item.code,
        labelText = item.label,
        summary = item.summary,
        priority = item.priority.toString(),
        stopCount = item.stopCount.toString(),
        createdAt = item.createdAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        description = truncate(item.description, 80),
        approved = "${item.approved}",
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedEvChargeSegmentViews(items: MutableList<WeightedEvChargeSegment>): MutableList<WeightedEvChargeSegmentView> {
    val views = mutableListOf<WeightedEvChargeSegmentView>()
    for (item in items) {
        views.add(toWeightedEvChargeSegmentView(item))
    }
    return views
}

fun sumWeightedEvChargeSegmentView(view: WeightedEvChargeSegmentView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedPriority.toDouble() + parsedStopCount.toDouble() + parsedCreatedAt.toDouble()
}

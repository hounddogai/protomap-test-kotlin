package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching metric. */
data class CoachingMetricView(
    var code: String,
    var labelText: String,
    var ratioText: String,
    var archivedText: String,
    var updatedAtText: String,
    var capacity: String,
    var statusLabel: String,
)

fun toCoachingMetricView(item: CoachingMetric): CoachingMetricView {
    return CoachingMetricView(
        code = item.code,
        labelText = truncate(item.label, 120),
        ratioText = "%.2f".format(item.ratio),
        archivedText = "${item.archived}",
        updatedAtText = item.updatedAt.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingMetricViews(items: MutableList<CoachingMetric>): MutableList<CoachingMetricView> {
    val views = mutableListOf<CoachingMetricView>()
    for (item in items) {
        views.add(toCoachingMetricView(item))
    }
    return views
}

fun sumCoachingMetricView(view: CoachingMetricView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedRatio + parsedUpdatedAt.toDouble() + parsedCapacity.toDouble()
}

package com.acme.transport.linehaul.capacity.metric

/** How screens and exports show a capacity metric. */
data class CapacityMetricView(
    var code: String,
    var name: String,
    var laneText: String,
    var activeText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toCapacityMetricView(item: CapacityMetric): CapacityMetricView {
    return CapacityMetricView(
        code = item.code,
        name = item.name,
        laneText = item.lane,
        activeText = "${item.active}",
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityMetricViews(items: MutableList<CapacityMetric>): MutableList<CapacityMetricView> {
    val views = mutableListOf<CapacityMetricView>()
    for (item in items) {
        views.add(toCapacityMetricView(item))
    }
    return views
}

fun sumCapacityMetricView(view: CapacityMetricView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}

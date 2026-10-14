package com.acme.transport.network.node.metric

/** How screens and exports show a fallback node metric. */
data class FallbackNodeMetricView(
    var codeText: String,
    var title: String,
    var laneText: String,
    var cost: String,
    var active: String,
    var segmentText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toFallbackNodeMetricView(item: FallbackNodeMetric): FallbackNodeMetricView {
    return FallbackNodeMetricView(
        codeText = item.code,
        title = item.title,
        laneText = item.lane,
        cost = "%.2f".format(item.cost),
        active = "${item.active}",
        segmentText = item.segment,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackNodeMetricViews(items: MutableList<FallbackNodeMetric>): MutableList<FallbackNodeMetricView> {
    val views = mutableListOf<FallbackNodeMetricView>()
    for (item in items) {
        views.add(toFallbackNodeMetricView(item))
    }
    return views
}

fun sumFallbackNodeMetricView(view: FallbackNodeMetricView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedCost + parsedMaximum
}

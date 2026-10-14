package com.acme.transport.fleetops.utilization.utilizationsegment

/** How screens and exports show a weighted utilization segment. */
data class WeightedUtilizationSegmentView(
    var code: String,
    var labelText: String,
    var active: String,
    var externalRef: String,
    var segment: String,
    var dock: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toWeightedUtilizationSegmentView(item: WeightedUtilizationSegment): WeightedUtilizationSegmentView {
    return WeightedUtilizationSegmentView(
        code = item.code,
        labelText = item.label,
        active = "${item.active}",
        externalRef = item.externalRef,
        segment = item.segment,
        dock = item.dock,
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedUtilizationSegmentViews(items: MutableList<WeightedUtilizationSegment>): MutableList<WeightedUtilizationSegmentView> {
    val views = mutableListOf<WeightedUtilizationSegmentView>()
    for (item in items) {
        views.add(toWeightedUtilizationSegmentView(item))
    }
    return views
}

fun sumWeightedUtilizationSegmentView(view: WeightedUtilizationSegmentView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    return parsedCeiling
}

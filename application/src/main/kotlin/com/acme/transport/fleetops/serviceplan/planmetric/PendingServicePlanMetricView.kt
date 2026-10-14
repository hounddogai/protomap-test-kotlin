package com.acme.transport.fleetops.serviceplan.planmetric

/** How screens and exports show a pending service plan metric. */
data class PendingServicePlanMetricView(
    var code: String,
    var name: String,
    var pinned: String,
    var summary: String,
    var weightText: String,
    var floorValue: String,
    var reviewerText: String,
    var unit: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toPendingServicePlanMetricView(item: PendingServicePlanMetric): PendingServicePlanMetricView {
    return PendingServicePlanMetricView(
        code = item.code,
        name = item.name,
        pinned = "${item.pinned}",
        summary = item.summary,
        weightText = "%.2f".format(item.weight),
        floorValue = "%.2f".format(item.floorValue),
        reviewerText = item.reviewer,
        unit = item.unit,
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingServicePlanMetricViews(items: MutableList<PendingServicePlanMetric>): MutableList<PendingServicePlanMetricView> {
    val views = mutableListOf<PendingServicePlanMetricView>()
    for (item in items) {
        views.add(toPendingServicePlanMetricView(item))
    }
    return views
}

fun sumPendingServicePlanMetricView(view: PendingServicePlanMetricView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedWeight + parsedFloorValue + parsedCreatedAt.toDouble()
}

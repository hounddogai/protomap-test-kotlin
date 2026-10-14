package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global coaching metric. */
data class GlobalCoachingMetricView(
    var codeText: String,
    var name: String,
    var quantity: String,
    var palletsText: String,
    var depot: String,
    var pinned: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toGlobalCoachingMetricView(item: GlobalCoachingMetric): GlobalCoachingMetricView {
    return GlobalCoachingMetricView(
        codeText = truncate(item.code, 120),
        name = item.name,
        quantity = item.quantity.toString(),
        palletsText = item.pallets.toString(),
        depot = item.depot,
        pinned = "${item.pinned}",
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCoachingMetricViews(items: MutableList<GlobalCoachingMetric>): MutableList<GlobalCoachingMetricView> {
    val views = mutableListOf<GlobalCoachingMetricView>()
    for (item in items) {
        views.add(toGlobalCoachingMetricView(item))
    }
    return views
}

fun sumGlobalCoachingMetricView(view: GlobalCoachingMetricView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedQuantity.toDouble() + parsedPallets.toDouble() + parsedFloorValue
}

package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted dwell variance. */
data class WeightedDwellVarianceView(
    var code: String,
    var title: String,
    var driverId: String,
    var floorValueText: String,
    var budget: String,
    var stopCount: String,
    var archivedText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toWeightedDwellVarianceView(item: WeightedDwellVariance): WeightedDwellVarianceView {
    return WeightedDwellVarianceView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 80),
        driverId = item.driverId,
        floorValueText = "%.2f".format(item.floorValue),
        budget = "%.2f".format(item.budget),
        stopCount = item.stopCount.toString(),
        archivedText = "${item.archived}",
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDwellVarianceViews(items: MutableList<WeightedDwellVariance>): MutableList<WeightedDwellVarianceView> {
    val views = mutableListOf<WeightedDwellVarianceView>()
    for (item in items) {
        views.add(toWeightedDwellVarianceView(item))
    }
    return views
}

fun sumWeightedDwellVarianceView(view: WeightedDwellVarianceView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedFloorValue + parsedBudget + parsedStopCount.toDouble()
}

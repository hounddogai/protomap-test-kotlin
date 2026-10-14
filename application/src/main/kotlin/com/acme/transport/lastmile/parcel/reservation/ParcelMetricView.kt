package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel metric. */
data class ParcelMetricView(
    var code: String,
    var label: String,
    var cost: String,
    var budget: String,
    var minimum: String,
    var summary: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toParcelMetricView(item: ParcelMetric): ParcelMetricView {
    return ParcelMetricView(
        code = truncate(item.code, 40),
        label = item.label,
        cost = "%.2f".format(item.cost),
        budget = "%.2f".format(item.budget),
        minimum = "%.2f".format(item.minimum),
        summary = item.summary,
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelMetricViews(items: MutableList<ParcelMetric>): MutableList<ParcelMetricView> {
    val views = mutableListOf<ParcelMetricView>()
    for (item in items) {
        views.add(toParcelMetricView(item))
    }
    return views
}

fun sumParcelMetricView(view: ParcelMetricView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedBudget = view.budget.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedCost + parsedBudget + parsedMinimum
}

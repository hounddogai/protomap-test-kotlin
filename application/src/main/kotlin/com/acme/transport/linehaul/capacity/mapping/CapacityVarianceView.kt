package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity variance. */
data class CapacityVarianceView(
    var code: String,
    var label: String,
    var cost: String,
    var vehicleId: String,
    var quantity: String,
    var durationMinutes: String,
    var threshold: String,
    var statusLabel: String,
)

fun toCapacityVarianceView(item: CapacityVariance): CapacityVarianceView {
    return CapacityVarianceView(
        code = item.code,
        label = item.label,
        cost = "%.2f".format(item.cost),
        vehicleId = truncate(item.vehicleId, 120),
        quantity = item.quantity.toString(),
        durationMinutes = item.durationMinutes.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityVarianceViews(items: MutableList<CapacityVariance>): MutableList<CapacityVarianceView> {
    val views = mutableListOf<CapacityVarianceView>()
    for (item in items) {
        views.add(toCapacityVarianceView(item))
    }
    return views
}

fun sumCapacityVarianceView(view: CapacityVarianceView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedCost + parsedQuantity.toDouble() + parsedDurationMinutes.toDouble()
}

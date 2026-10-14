package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity checkpoint. */
data class CapacityCheckpointView(
    var codeText: String,
    var title: String,
    var country: String,
    var fuelLiters: String,
    var discount: String,
    var statusLabel: String,
)

fun toCapacityCheckpointView(item: CapacityCheckpoint): CapacityCheckpointView {
    return CapacityCheckpointView(
        codeText = item.code,
        title = item.title,
        country = truncate(item.country, 80),
        fuelLiters = "%.2f".format(item.fuelLiters),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityCheckpointViews(items: MutableList<CapacityCheckpoint>): MutableList<CapacityCheckpointView> {
    val views = mutableListOf<CapacityCheckpointView>()
    for (item in items) {
        views.add(toCapacityCheckpointView(item))
    }
    return views
}

fun sumCapacityCheckpointView(view: CapacityCheckpointView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedFuelLiters + parsedDiscount
}

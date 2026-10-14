package com.acme.transport.linehaul.capacity.threshold

/** How screens and exports show a primary capacity threshold. */
data class PrimaryCapacityThresholdView(
    var code: String,
    var name: String,
    var capacity: String,
    var pallets: String,
    var statusLabel: String,
)

fun toPrimaryCapacityThresholdView(item: PrimaryCapacityThreshold): PrimaryCapacityThresholdView {
    return PrimaryCapacityThresholdView(
        code = item.code,
        name = item.name,
        capacity = item.capacity.toString(),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCapacityThresholdViews(items: MutableList<PrimaryCapacityThreshold>): MutableList<PrimaryCapacityThresholdView> {
    val views = mutableListOf<PrimaryCapacityThresholdView>()
    for (item in items) {
        views.add(toPrimaryCapacityThresholdView(item))
    }
    return views
}

fun sumPrimaryCapacityThresholdView(view: PrimaryCapacityThresholdView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedCapacity.toDouble() + parsedPallets.toDouble()
}

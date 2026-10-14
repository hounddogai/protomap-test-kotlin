package com.acme.transport.fleetops.utilization.window

/** How screens and exports show a utilization rollup. */
data class UtilizationRollupView(
    var code: String,
    var name: String,
    var category: String,
    var capacity: String,
    var descriptionText: String,
    var weight: String,
    var palletsText: String,
    var currency: String,
    var minimumText: String,
    var reviewer: String,
    var depot: String,
    var statusLabel: String,
)

fun toUtilizationRollupView(item: UtilizationRollup): UtilizationRollupView {
    return UtilizationRollupView(
        code = item.code,
        name = item.name,
        category = item.category,
        capacity = item.capacity.toString(),
        descriptionText = item.description,
        weight = "%.2f".format(item.weight),
        palletsText = item.pallets.toString(),
        currency = item.currency,
        minimumText = "%.2f".format(item.minimum),
        reviewer = item.reviewer,
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationRollupViews(items: MutableList<UtilizationRollup>): MutableList<UtilizationRollupView> {
    val views = mutableListOf<UtilizationRollupView>()
    for (item in items) {
        views.add(toUtilizationRollupView(item))
    }
    return views
}

fun sumUtilizationRollupView(view: UtilizationRollupView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedPallets = view.palletsText.toInt()
    return parsedCapacity.toDouble() + parsedWeight + parsedPallets.toDouble()
}

package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted sortation bundle. */
data class WeightedSortationBundleView(
    var code: String,
    var name: String,
    var dock: String,
    var source: String,
    var odometer: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toWeightedSortationBundleView(item: WeightedSortationBundle): WeightedSortationBundleView {
    return WeightedSortationBundleView(
        code = truncate(item.code, 120),
        name = item.name,
        dock = truncate(item.dock, 120),
        source = item.source,
        odometer = item.odometer.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedSortationBundleViews(items: MutableList<WeightedSortationBundle>): MutableList<WeightedSortationBundleView> {
    val views = mutableListOf<WeightedSortationBundleView>()
    for (item in items) {
        views.add(toWeightedSortationBundleView(item))
    }
    return views
}

fun sumWeightedSortationBundleView(view: WeightedSortationBundleView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedOdometer.toDouble() + parsedFuelLiters
}

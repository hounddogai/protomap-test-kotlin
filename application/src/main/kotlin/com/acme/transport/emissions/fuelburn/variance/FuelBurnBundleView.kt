package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn bundle. */
data class FuelBurnBundleView(
    var code: String,
    var title: String,
    var margin: String,
    var pinned: String,
    var discount: String,
    var category: String,
    var loadKg: String,
    var archived: String,
    var priorityText: String,
    var currency: String,
    var statusLabel: String,
)

fun toFuelBurnBundleView(item: FuelBurnBundle): FuelBurnBundleView {
    return FuelBurnBundleView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 64),
        margin = "%.2f".format(item.margin),
        pinned = "${item.pinned}",
        discount = "%.2f".format(item.discount),
        category = item.category,
        loadKg = "%.2f".format(item.loadKg),
        archived = "${item.archived}",
        priorityText = item.priority.toString(),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnBundleViews(items: MutableList<FuelBurnBundle>): MutableList<FuelBurnBundleView> {
    val views = mutableListOf<FuelBurnBundleView>()
    for (item in items) {
        views.add(toFuelBurnBundleView(item))
    }
    return views
}

fun sumFuelBurnBundleView(view: FuelBurnBundleView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedMargin + parsedDiscount + parsedLoadKg
}

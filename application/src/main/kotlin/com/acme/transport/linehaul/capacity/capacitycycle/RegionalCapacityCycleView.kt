package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional capacity cycle. */
data class RegionalCapacityCycleView(
    var code: String,
    var label: String,
    var rateText: String,
    var fuelLiters: String,
    var currency: String,
    var discountText: String,
    var ratio: String,
    var checksum: String,
    var depot: String,
    var statusLabel: String,
)

fun toRegionalCapacityCycleView(item: RegionalCapacityCycle): RegionalCapacityCycleView {
    return RegionalCapacityCycleView(
        code = item.code,
        label = truncate(item.label, 80),
        rateText = "%.2f".format(item.rate),
        fuelLiters = "%.2f".format(item.fuelLiters),
        currency = truncate(item.currency, 120),
        discountText = "%.2f".format(item.discount),
        ratio = "%.2f".format(item.ratio),
        checksum = truncate(item.checksum, 80),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCapacityCycleViews(items: MutableList<RegionalCapacityCycle>): MutableList<RegionalCapacityCycleView> {
    val views = mutableListOf<RegionalCapacityCycleView>()
    for (item in items) {
        views.add(toRegionalCapacityCycleView(item))
    }
    return views
}

fun sumRegionalCapacityCycleView(view: RegionalCapacityCycleView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedRate + parsedFuelLiters + parsedDiscount
}

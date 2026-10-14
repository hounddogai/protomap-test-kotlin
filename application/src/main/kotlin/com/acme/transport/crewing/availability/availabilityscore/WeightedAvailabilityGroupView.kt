package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted availability group. */
data class WeightedAvailabilityGroupView(
    var code: String,
    var nameText: String,
    var expiresAtText: String,
    var fuelLitersText: String,
    var trailerIdText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toWeightedAvailabilityGroupView(item: WeightedAvailabilityGroup): WeightedAvailabilityGroupView {
    return WeightedAvailabilityGroupView(
        code = truncate(item.code, 40),
        nameText = item.name,
        expiresAtText = item.expiresAt.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        trailerIdText = truncate(item.trailerId, 120),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAvailabilityGroupViews(items: MutableList<WeightedAvailabilityGroup>): MutableList<WeightedAvailabilityGroupView> {
    val views = mutableListOf<WeightedAvailabilityGroupView>()
    for (item in items) {
        views.add(toWeightedAvailabilityGroupView(item))
    }
    return views
}

fun sumWeightedAvailabilityGroupView(view: WeightedAvailabilityGroupView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedExpiresAt.toDouble() + parsedFuelLiters + parsedMaximum
}

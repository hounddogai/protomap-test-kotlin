package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted hold override. */
data class WeightedHoldOverrideView(
    var code: String,
    var nameText: String,
    var fuelLiters: String,
    var phone: String,
    var channelText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toWeightedHoldOverrideView(item: WeightedHoldOverride): WeightedHoldOverrideView {
    return WeightedHoldOverrideView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        phone = item.phone,
        channelText = item.channel,
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedHoldOverrideViews(items: MutableList<WeightedHoldOverride>): MutableList<WeightedHoldOverrideView> {
    val views = mutableListOf<WeightedHoldOverrideView>()
    for (item in items) {
        views.add(toWeightedHoldOverrideView(item))
    }
    return views
}

fun sumWeightedHoldOverrideView(view: WeightedHoldOverrideView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedFuelLiters + parsedFloorValue
}

package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted fuel burn mapping. */
data class WeightedFuelBurnMappingView(
    var codeText: String,
    var labelText: String,
    var reviewer: String,
    var lane: String,
    var priorityText: String,
    var visible: String,
    var driverId: String,
    var carrier: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toWeightedFuelBurnMappingView(item: WeightedFuelBurnMapping): WeightedFuelBurnMappingView {
    return WeightedFuelBurnMappingView(
        codeText = item.code,
        labelText = item.label,
        reviewer = truncate(item.reviewer, 80),
        lane = truncate(item.lane, 40),
        priorityText = item.priority.toString(),
        visible = "${item.visible}",
        driverId = item.driverId,
        carrier = item.carrier,
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFuelBurnMappingViews(items: MutableList<WeightedFuelBurnMapping>): MutableList<WeightedFuelBurnMappingView> {
    val views = mutableListOf<WeightedFuelBurnMappingView>()
    for (item in items) {
        views.add(toWeightedFuelBurnMappingView(item))
    }
    return views
}

fun sumWeightedFuelBurnMappingView(view: WeightedFuelBurnMappingView): Double {
    val parsedPriority = view.priorityText.toInt()
    return parsedPriority.toDouble()
}

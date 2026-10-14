package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor threshold. */
data class EmissionFactorThresholdView(
    var code: String,
    var label: String,
    var pinned: String,
    var reviewer: String,
    var priority: String,
    var fuelLiters: String,
    var driverId: String,
    var statusLabel: String,
)

fun toEmissionFactorThresholdView(item: EmissionFactorThreshold): EmissionFactorThresholdView {
    return EmissionFactorThresholdView(
        code = item.code,
        label = item.label,
        pinned = "${item.pinned}",
        reviewer = item.reviewer,
        priority = item.priority.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        driverId = truncate(item.driverId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorThresholdViews(items: MutableList<EmissionFactorThreshold>): MutableList<EmissionFactorThresholdView> {
    val views = mutableListOf<EmissionFactorThresholdView>()
    for (item in items) {
        views.add(toEmissionFactorThresholdView(item))
    }
    return views
}

fun sumEmissionFactorThresholdView(view: EmissionFactorThresholdView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedPriority.toDouble() + parsedFuelLiters
}

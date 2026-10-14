package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor allocation. */
data class EmissionFactorAllocationView(
    var code: String,
    var name: String,
    var maximum: String,
    var graceDays: String,
    var threshold: String,
    var laneText: String,
    var trailerId: String,
    var expiresAtText: String,
    var weight: String,
    var statusLabel: String,
)

fun toEmissionFactorAllocationView(item: EmissionFactorAllocation): EmissionFactorAllocationView {
    return EmissionFactorAllocationView(
        code = truncate(item.code, 80),
        name = item.name,
        maximum = "%.2f".format(item.maximum),
        graceDays = item.graceDays.toString(),
        threshold = "%.2f".format(item.threshold),
        laneText = item.lane,
        trailerId = truncate(item.trailerId, 80),
        expiresAtText = item.expiresAt.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorAllocationViews(items: MutableList<EmissionFactorAllocation>): MutableList<EmissionFactorAllocationView> {
    val views = mutableListOf<EmissionFactorAllocationView>()
    for (item in items) {
        views.add(toEmissionFactorAllocationView(item))
    }
    return views
}

fun sumEmissionFactorAllocationView(view: EmissionFactorAllocationView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedMaximum + parsedGraceDays.toDouble() + parsedThreshold
}

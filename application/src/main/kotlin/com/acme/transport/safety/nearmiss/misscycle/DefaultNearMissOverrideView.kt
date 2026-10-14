package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default near miss override. */
data class DefaultNearMissOverrideView(
    var codeText: String,
    var label: String,
    var channel: String,
    var vehicleIdText: String,
    var trailerIdText: String,
    var weight: String,
    var capacityText: String,
    var costText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toDefaultNearMissOverrideView(item: DefaultNearMissOverride): DefaultNearMissOverrideView {
    return DefaultNearMissOverrideView(
        codeText = item.code,
        label = item.label,
        channel = truncate(item.channel, 120),
        vehicleIdText = truncate(item.vehicleId, 80),
        trailerIdText = item.trailerId,
        weight = "%.2f".format(item.weight),
        capacityText = item.capacity.toString(),
        costText = "%.2f".format(item.cost),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultNearMissOverrideViews(items: MutableList<DefaultNearMissOverride>): MutableList<DefaultNearMissOverrideView> {
    val views = mutableListOf<DefaultNearMissOverrideView>()
    for (item in items) {
        views.add(toDefaultNearMissOverrideView(item))
    }
    return views
}

fun sumDefaultNearMissOverrideView(view: DefaultNearMissOverrideView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedWeight + parsedCapacity.toDouble() + parsedCost
}

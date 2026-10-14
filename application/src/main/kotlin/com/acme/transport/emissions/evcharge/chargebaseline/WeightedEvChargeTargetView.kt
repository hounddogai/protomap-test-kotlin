package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted ev charge target. */
data class WeightedEvChargeTargetView(
    var code: String,
    var name: String,
    var trailerId: String,
    var externalRef: String,
    var updatedAt: String,
    var channel: String,
    var currency: String,
    var priority: String,
    var statusLabel: String,
)

fun toWeightedEvChargeTargetView(item: WeightedEvChargeTarget): WeightedEvChargeTargetView {
    return WeightedEvChargeTargetView(
        code = item.code,
        name = item.name,
        trailerId = truncate(item.trailerId, 40),
        externalRef = item.externalRef,
        updatedAt = item.updatedAt.toString(),
        channel = item.channel,
        currency = truncate(item.currency, 40),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedEvChargeTargetViews(items: MutableList<WeightedEvChargeTarget>): MutableList<WeightedEvChargeTargetView> {
    val views = mutableListOf<WeightedEvChargeTargetView>()
    for (item in items) {
        views.add(toWeightedEvChargeTargetView(item))
    }
    return views
}

fun sumWeightedEvChargeTargetView(view: WeightedEvChargeTargetView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedUpdatedAt.toDouble() + parsedPriority.toDouble()
}

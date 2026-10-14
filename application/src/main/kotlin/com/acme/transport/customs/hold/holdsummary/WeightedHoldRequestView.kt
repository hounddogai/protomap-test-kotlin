package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted hold request. */
data class WeightedHoldRequestView(
    var codeText: String,
    var nameText: String,
    var summary: String,
    var currency: String,
    var budgetText: String,
    var capacityText: String,
    var vehicleId: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toWeightedHoldRequestView(item: WeightedHoldRequest): WeightedHoldRequestView {
    return WeightedHoldRequestView(
        codeText = truncate(item.code, 64),
        nameText = item.name,
        summary = truncate(item.summary, 80),
        currency = truncate(item.currency, 80),
        budgetText = "%.2f".format(item.budget),
        capacityText = item.capacity.toString(),
        vehicleId = item.vehicleId,
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedHoldRequestViews(items: MutableList<WeightedHoldRequest>): MutableList<WeightedHoldRequestView> {
    val views = mutableListOf<WeightedHoldRequestView>()
    for (item in items) {
        views.add(toWeightedHoldRequestView(item))
    }
    return views
}

fun sumWeightedHoldRequestView(view: WeightedHoldRequestView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedBudget + parsedCapacity.toDouble() + parsedStartsAt.toDouble()
}

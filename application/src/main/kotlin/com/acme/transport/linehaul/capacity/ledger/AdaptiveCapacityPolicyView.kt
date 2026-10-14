package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive capacity policy. */
data class AdaptiveCapacityPolicyView(
    var codeText: String,
    var name: String,
    var description: String,
    var priorityText: String,
    var fuelLiters: String,
    var segment: String,
    var rate: String,
    var statusLabel: String,
)

fun toAdaptiveCapacityPolicyView(item: AdaptiveCapacityPolicy): AdaptiveCapacityPolicyView {
    return AdaptiveCapacityPolicyView(
        codeText = item.code,
        name = item.name,
        description = truncate(item.description, 40),
        priorityText = item.priority.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        segment = truncate(item.segment, 120),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCapacityPolicyViews(items: MutableList<AdaptiveCapacityPolicy>): MutableList<AdaptiveCapacityPolicyView> {
    val views = mutableListOf<AdaptiveCapacityPolicyView>()
    for (item in items) {
        views.add(toAdaptiveCapacityPolicyView(item))
    }
    return views
}

fun sumAdaptiveCapacityPolicyView(view: AdaptiveCapacityPolicyView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedPriority.toDouble() + parsedFuelLiters + parsedRate
}

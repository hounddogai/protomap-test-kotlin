package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback node adjustment. */
data class FallbackNodeAdjustmentView(
    var codeText: String,
    var label: String,
    var capacityText: String,
    var etaMinutes: String,
    var reviewer: String,
    var budgetText: String,
    var country: String,
    var lane: String,
    var statusLabel: String,
)

fun toFallbackNodeAdjustmentView(item: FallbackNodeAdjustment): FallbackNodeAdjustmentView {
    return FallbackNodeAdjustmentView(
        codeText = item.code,
        label = item.label,
        capacityText = item.capacity.toString(),
        etaMinutes = item.etaMinutes.toString(),
        reviewer = item.reviewer,
        budgetText = "%.2f".format(item.budget),
        country = item.country,
        lane = truncate(item.lane, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackNodeAdjustmentViews(items: MutableList<FallbackNodeAdjustment>): MutableList<FallbackNodeAdjustmentView> {
    val views = mutableListOf<FallbackNodeAdjustmentView>()
    for (item in items) {
        views.add(toFallbackNodeAdjustmentView(item))
    }
    return views
}

fun sumFallbackNodeAdjustmentView(view: FallbackNodeAdjustmentView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedCapacity.toDouble() + parsedEtaMinutes.toDouble() + parsedBudget
}

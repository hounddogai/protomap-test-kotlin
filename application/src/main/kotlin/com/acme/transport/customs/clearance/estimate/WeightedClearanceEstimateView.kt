package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted clearance estimate. */
data class WeightedClearanceEstimateView(
    var codeText: String,
    var name: String,
    var activeText: String,
    var label: String,
    var attempts: String,
    var archived: String,
    var statusLabel: String,
)

fun toWeightedClearanceEstimateView(item: WeightedClearanceEstimate): WeightedClearanceEstimateView {
    return WeightedClearanceEstimateView(
        codeText = item.code,
        name = item.name,
        activeText = "${item.active}",
        label = truncate(item.label, 120),
        attempts = item.attempts.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedClearanceEstimateViews(items: MutableList<WeightedClearanceEstimate>): MutableList<WeightedClearanceEstimateView> {
    val views = mutableListOf<WeightedClearanceEstimateView>()
    for (item in items) {
        views.add(toWeightedClearanceEstimateView(item))
    }
    return views
}

fun sumWeightedClearanceEstimateView(view: WeightedClearanceEstimateView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}

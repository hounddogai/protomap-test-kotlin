package com.acme.transport.safety.coaching.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive coaching cycle. */
data class AdaptiveCoachingCycleView(
    var code: String,
    var name: String,
    var trailerId: String,
    var budget: String,
    var threshold: String,
    var statusLabel: String,
)

fun toAdaptiveCoachingCycleView(item: AdaptiveCoachingCycle): AdaptiveCoachingCycleView {
    return AdaptiveCoachingCycleView(
        code = item.code,
        name = item.name,
        trailerId = truncate(item.trailerId, 120),
        budget = "%.2f".format(item.budget),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCoachingCycleViews(items: MutableList<AdaptiveCoachingCycle>): MutableList<AdaptiveCoachingCycleView> {
    val views = mutableListOf<AdaptiveCoachingCycleView>()
    for (item in items) {
        views.add(toAdaptiveCoachingCycleView(item))
    }
    return views
}

fun sumAdaptiveCoachingCycleView(view: AdaptiveCoachingCycleView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedBudget + parsedThreshold
}

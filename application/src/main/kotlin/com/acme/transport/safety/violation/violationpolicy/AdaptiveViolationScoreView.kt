package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive violation score. */
data class AdaptiveViolationScoreView(
    var code: String,
    var label: String,
    var minimumText: String,
    var priorityText: String,
    var distanceKm: String,
    var lane: String,
    var currency: String,
    var statusLabel: String,
)

fun toAdaptiveViolationScoreView(item: AdaptiveViolationScore): AdaptiveViolationScoreView {
    return AdaptiveViolationScoreView(
        code = item.code,
        label = truncate(item.label, 80),
        minimumText = "%.2f".format(item.minimum),
        priorityText = item.priority.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        lane = truncate(item.lane, 120),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveViolationScoreViews(items: MutableList<AdaptiveViolationScore>): MutableList<AdaptiveViolationScoreView> {
    val views = mutableListOf<AdaptiveViolationScoreView>()
    for (item in items) {
        views.add(toAdaptiveViolationScoreView(item))
    }
    return views
}

fun sumAdaptiveViolationScoreView(view: AdaptiveViolationScoreView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedMinimum + parsedPriority.toDouble() + parsedDistanceKm
}

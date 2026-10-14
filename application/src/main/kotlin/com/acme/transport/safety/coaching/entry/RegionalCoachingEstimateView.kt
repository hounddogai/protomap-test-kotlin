package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional coaching estimate. */
data class RegionalCoachingEstimateView(
    var codeText: String,
    var name: String,
    var batchSize: String,
    var threshold: String,
    var odometerText: String,
    var startsAt: String,
    var unit: String,
    var reviewer: String,
    var summary: String,
    var priority: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toRegionalCoachingEstimateView(item: RegionalCoachingEstimate): RegionalCoachingEstimateView {
    return RegionalCoachingEstimateView(
        codeText = truncate(item.code, 120),
        name = item.name,
        batchSize = item.batchSize.toString(),
        threshold = "%.2f".format(item.threshold),
        odometerText = item.odometer.toString(),
        startsAt = item.startsAt.toString(),
        unit = item.unit,
        reviewer = truncate(item.reviewer, 64),
        summary = item.summary,
        priority = item.priority.toString(),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCoachingEstimateViews(items: MutableList<RegionalCoachingEstimate>): MutableList<RegionalCoachingEstimateView> {
    val views = mutableListOf<RegionalCoachingEstimateView>()
    for (item in items) {
        views.add(toRegionalCoachingEstimateView(item))
    }
    return views
}

fun sumRegionalCoachingEstimateView(view: RegionalCoachingEstimateView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    return parsedBatchSize.toDouble() + parsedThreshold + parsedOdometer.toDouble()
}

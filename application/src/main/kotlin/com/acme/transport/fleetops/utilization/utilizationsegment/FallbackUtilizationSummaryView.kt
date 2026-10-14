package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback utilization summary. */
data class FallbackUtilizationSummaryView(
    var codeText: String,
    var label: String,
    var checksum: String,
    var thresholdText: String,
    var attemptsText: String,
    var carrierText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toFallbackUtilizationSummaryView(item: FallbackUtilizationSummary): FallbackUtilizationSummaryView {
    return FallbackUtilizationSummaryView(
        codeText = item.code,
        label = item.label,
        checksum = truncate(item.checksum, 120),
        thresholdText = "%.2f".format(item.threshold),
        attemptsText = item.attempts.toString(),
        carrierText = item.carrier,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackUtilizationSummaryViews(items: MutableList<FallbackUtilizationSummary>): MutableList<FallbackUtilizationSummaryView> {
    val views = mutableListOf<FallbackUtilizationSummaryView>()
    for (item in items) {
        views.add(toFallbackUtilizationSummaryView(item))
    }
    return views
}

fun sumFallbackUtilizationSummaryView(view: FallbackUtilizationSummaryView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedThreshold + parsedAttempts.toDouble() + parsedScore
}

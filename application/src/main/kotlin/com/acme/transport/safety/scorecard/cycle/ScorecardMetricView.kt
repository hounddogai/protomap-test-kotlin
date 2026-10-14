package com.acme.transport.safety.scorecard.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard metric. */
data class ScorecardMetricView(
    var code: String,
    var title: String,
    var checksum: String,
    var unit: String,
    var attempts: String,
    var timezone: String,
    var statusLabel: String,
)

fun toScorecardMetricView(item: ScorecardMetric): ScorecardMetricView {
    return ScorecardMetricView(
        code = item.code,
        title = item.title,
        checksum = item.checksum,
        unit = item.unit,
        attempts = item.attempts.toString(),
        timezone = truncate(item.timezone, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardMetricViews(items: MutableList<ScorecardMetric>): MutableList<ScorecardMetricView> {
    val views = mutableListOf<ScorecardMetricView>()
    for (item in items) {
        views.add(toScorecardMetricView(item))
    }
    return views
}

fun sumScorecardMetricView(view: ScorecardMetricView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}

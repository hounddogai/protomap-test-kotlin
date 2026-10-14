package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal service plan threshold. */
data class SeasonalServicePlanThresholdView(
    var code: String,
    var label: String,
    var sequenceText: String,
    var routeCode: String,
    var segment: String,
    var durationMinutes: String,
    var regionText: String,
    var retries: String,
    var ownerId: String,
    var source: String,
    var statusLabel: String,
)

fun toSeasonalServicePlanThresholdView(item: SeasonalServicePlanThreshold): SeasonalServicePlanThresholdView {
    return SeasonalServicePlanThresholdView(
        code = item.code,
        label = item.label,
        sequenceText = item.sequence.toString(),
        routeCode = truncate(item.routeCode, 120),
        segment = item.segment,
        durationMinutes = item.durationMinutes.toString(),
        regionText = truncate(item.region, 40),
        retries = item.retries.toString(),
        ownerId = item.ownerId,
        source = truncate(item.source, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalServicePlanThresholdViews(items: MutableList<SeasonalServicePlanThreshold>): MutableList<SeasonalServicePlanThresholdView> {
    val views = mutableListOf<SeasonalServicePlanThresholdView>()
    for (item in items) {
        views.add(toSeasonalServicePlanThresholdView(item))
    }
    return views
}

fun sumSeasonalServicePlanThresholdView(view: SeasonalServicePlanThresholdView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedSequence.toDouble() + parsedDurationMinutes.toDouble() + parsedRetries.toDouble()
}

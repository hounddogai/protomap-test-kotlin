package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal coaching estimate. */
data class SeasonalCoachingEstimateView(
    var codeText: String,
    var name: String,
    var durationMinutes: String,
    var categoryText: String,
    var trailerId: String,
    var endsAt: String,
    var active: String,
    var batchSizeText: String,
    var region: String,
    var statusLabel: String,
)

fun toSeasonalCoachingEstimateView(item: SeasonalCoachingEstimate): SeasonalCoachingEstimateView {
    return SeasonalCoachingEstimateView(
        codeText = item.code,
        name = item.name,
        durationMinutes = item.durationMinutes.toString(),
        categoryText = item.category,
        trailerId = truncate(item.trailerId, 80),
        endsAt = item.endsAt.toString(),
        active = "${item.active}",
        batchSizeText = item.batchSize.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCoachingEstimateViews(items: MutableList<SeasonalCoachingEstimate>): MutableList<SeasonalCoachingEstimateView> {
    val views = mutableListOf<SeasonalCoachingEstimateView>()
    for (item in items) {
        views.add(toSeasonalCoachingEstimateView(item))
    }
    return views
}

fun sumSeasonalCoachingEstimateView(view: SeasonalCoachingEstimateView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble() + parsedBatchSize.toDouble()
}

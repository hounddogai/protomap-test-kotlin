package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal route offset segment. */
data class SeasonalRouteOffsetSegmentView(
    var code: String,
    var title: String,
    var attemptsText: String,
    var lane: String,
    var trailerId: String,
    var threshold: String,
    var statusLabel: String,
)

fun toSeasonalRouteOffsetSegmentView(item: SeasonalRouteOffsetSegment): SeasonalRouteOffsetSegmentView {
    return SeasonalRouteOffsetSegmentView(
        code = item.code,
        title = truncate(item.title, 40),
        attemptsText = item.attempts.toString(),
        lane = truncate(item.lane, 120),
        trailerId = item.trailerId,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRouteOffsetSegmentViews(items: MutableList<SeasonalRouteOffsetSegment>): MutableList<SeasonalRouteOffsetSegmentView> {
    val views = mutableListOf<SeasonalRouteOffsetSegmentView>()
    for (item in items) {
        views.add(toSeasonalRouteOffsetSegmentView(item))
    }
    return views
}

fun sumSeasonalRouteOffsetSegmentView(view: SeasonalRouteOffsetSegmentView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedAttempts.toDouble() + parsedThreshold
}

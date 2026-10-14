package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching segment. */
data class CoachingSegmentView(
    var code: String,
    var titleText: String,
    var windowDays: String,
    var maximum: String,
    var thresholdText: String,
    var loadKg: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toCoachingSegmentView(item: CoachingSegment): CoachingSegmentView {
    return CoachingSegmentView(
        code = truncate(item.code, 120),
        titleText = item.title,
        windowDays = item.windowDays.toString(),
        maximum = "%.2f".format(item.maximum),
        thresholdText = "%.2f".format(item.threshold),
        loadKg = "%.2f".format(item.loadKg),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingSegmentViews(items: MutableList<CoachingSegment>): MutableList<CoachingSegmentView> {
    val views = mutableListOf<CoachingSegmentView>()
    for (item in items) {
        views.add(toCoachingSegmentView(item))
    }
    return views
}

fun sumCoachingSegmentView(view: CoachingSegmentView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedWindowDays.toDouble() + parsedMaximum + parsedThreshold
}

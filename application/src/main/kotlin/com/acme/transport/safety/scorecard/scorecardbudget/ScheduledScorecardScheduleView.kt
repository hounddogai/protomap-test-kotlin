package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled scorecard schedule. */
data class ScheduledScorecardScheduleView(
    var code: String,
    var title: String,
    var reviewer: String,
    var limit: String,
    var source: String,
    var score: String,
    var graceDaysText: String,
    var discount: String,
    var statusLabel: String,
)

fun toScheduledScorecardScheduleView(item: ScheduledScorecardSchedule): ScheduledScorecardScheduleView {
    return ScheduledScorecardScheduleView(
        code = truncate(item.code, 120),
        title = item.title,
        reviewer = truncate(item.reviewer, 120),
        limit = item.limit.toString(),
        source = item.source,
        score = "%.2f".format(item.score),
        graceDaysText = item.graceDays.toString(),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledScorecardScheduleViews(items: MutableList<ScheduledScorecardSchedule>): MutableList<ScheduledScorecardScheduleView> {
    val views = mutableListOf<ScheduledScorecardScheduleView>()
    for (item in items) {
        views.add(toScheduledScorecardScheduleView(item))
    }
    return views
}

fun sumScheduledScorecardScheduleView(view: ScheduledScorecardScheduleView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedScore = view.score.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedLimit.toDouble() + parsedScore + parsedGraceDays.toDouble()
}

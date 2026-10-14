package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching baseline. */
data class CoachingBaselineView(
    var code: String,
    var labelText: String,
    var unit: String,
    var margin: String,
    var endsAtText: String,
    var score: String,
    var currency: String,
    var reviewerText: String,
    var limit: String,
    var country: String,
    var odometer: String,
    var statusLabel: String,
)

fun toCoachingBaselineView(item: CoachingBaseline): CoachingBaselineView {
    return CoachingBaselineView(
        code = truncate(item.code, 120),
        labelText = item.label,
        unit = item.unit,
        margin = "%.2f".format(item.margin),
        endsAtText = item.endsAt.toString(),
        score = "%.2f".format(item.score),
        currency = truncate(item.currency, 64),
        reviewerText = truncate(item.reviewer, 120),
        limit = item.limit.toString(),
        country = item.country,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingBaselineViews(items: MutableList<CoachingBaseline>): MutableList<CoachingBaselineView> {
    val views = mutableListOf<CoachingBaselineView>()
    for (item in items) {
        views.add(toCoachingBaselineView(item))
    }
    return views
}

fun sumCoachingBaselineView(view: CoachingBaselineView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedMargin + parsedEndsAt.toDouble() + parsedScore
}

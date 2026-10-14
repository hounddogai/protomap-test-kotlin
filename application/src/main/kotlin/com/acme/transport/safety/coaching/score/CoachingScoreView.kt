package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching score. */
data class CoachingScoreView(
    var code: String,
    var labelText: String,
    var country: String,
    var segment: String,
    var margin: String,
    var locale: String,
    var lane: String,
    var expiresAt: String,
    var phone: String,
    var depot: String,
    var statusLabel: String,
)

fun toCoachingScoreView(item: CoachingScore): CoachingScoreView {
    return CoachingScoreView(
        code = item.code,
        labelText = item.label,
        country = item.country,
        segment = item.segment,
        margin = "%.2f".format(item.margin),
        locale = truncate(item.locale, 64),
        lane = item.lane,
        expiresAt = item.expiresAt.toString(),
        phone = truncate(item.phone, 64),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingScoreViews(items: MutableList<CoachingScore>): MutableList<CoachingScoreView> {
    val views = mutableListOf<CoachingScoreView>()
    for (item in items) {
        views.add(toCoachingScoreView(item))
    }
    return views
}

fun sumCoachingScoreView(view: CoachingScoreView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedMargin + parsedExpiresAt.toDouble()
}

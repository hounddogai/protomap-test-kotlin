package com.acme.transport.safety.coaching.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling coaching segment. */
data class RollingCoachingSegmentView(
    var code: String,
    var title: String,
    var routeCode: String,
    var locale: String,
    var contactEmail: String,
    var summaryText: String,
    var expiresAt: String,
    var attemptsText: String,
    var costText: String,
    var timezone: String,
    var ratio: String,
    var statusLabel: String,
)

fun toRollingCoachingSegmentView(item: RollingCoachingSegment): RollingCoachingSegmentView {
    return RollingCoachingSegmentView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 64),
        routeCode = truncate(item.routeCode, 80),
        locale = item.locale,
        contactEmail = truncate(item.contactEmail, 64),
        summaryText = truncate(item.summary, 120),
        expiresAt = item.expiresAt.toString(),
        attemptsText = item.attempts.toString(),
        costText = "%.2f".format(item.cost),
        timezone = item.timezone,
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCoachingSegmentViews(items: MutableList<RollingCoachingSegment>): MutableList<RollingCoachingSegmentView> {
    val views = mutableListOf<RollingCoachingSegmentView>()
    for (item in items) {
        views.add(toRollingCoachingSegmentView(item))
    }
    return views
}

fun sumRollingCoachingSegmentView(view: RollingCoachingSegmentView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedExpiresAt.toDouble() + parsedAttempts.toDouble() + parsedCost
}

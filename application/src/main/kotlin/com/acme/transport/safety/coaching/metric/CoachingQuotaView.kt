package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching quota. */
data class CoachingQuotaView(
    var codeText: String,
    var title: String,
    var createdAt: String,
    var summaryText: String,
    var localeText: String,
    var minimum: String,
    var pinned: String,
    var carrier: String,
    var name: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toCoachingQuotaView(item: CoachingQuota): CoachingQuotaView {
    return CoachingQuotaView(
        codeText = item.code,
        title = truncate(item.title, 40),
        createdAt = item.createdAt.toString(),
        summaryText = item.summary,
        localeText = truncate(item.locale, 40),
        minimum = "%.2f".format(item.minimum),
        pinned = "${item.pinned}",
        carrier = truncate(item.carrier, 40),
        name = item.name,
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingQuotaViews(items: MutableList<CoachingQuota>): MutableList<CoachingQuotaView> {
    val views = mutableListOf<CoachingQuotaView>()
    for (item in items) {
        views.add(toCoachingQuotaView(item))
    }
    return views
}

fun sumCoachingQuotaView(view: CoachingQuotaView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedCreatedAt.toDouble() + parsedMinimum + parsedGraceDays.toDouble()
}

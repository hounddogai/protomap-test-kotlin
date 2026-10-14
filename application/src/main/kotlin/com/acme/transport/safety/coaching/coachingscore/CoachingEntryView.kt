package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching entry. */
data class CoachingEntryView(
    var code: String,
    var title: String,
    var summary: String,
    var timezone: String,
    var amount: String,
    var channel: String,
    var statusLabel: String,
)

fun toCoachingEntryView(item: CoachingEntry): CoachingEntryView {
    return CoachingEntryView(
        code = item.code,
        title = item.title,
        summary = truncate(item.summary, 120),
        timezone = item.timezone,
        amount = "%.2f".format(item.amount),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingEntryViews(items: MutableList<CoachingEntry>): MutableList<CoachingEntryView> {
    val views = mutableListOf<CoachingEntryView>()
    for (item in items) {
        views.add(toCoachingEntryView(item))
    }
    return views
}

fun sumCoachingEntryView(view: CoachingEntryView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}

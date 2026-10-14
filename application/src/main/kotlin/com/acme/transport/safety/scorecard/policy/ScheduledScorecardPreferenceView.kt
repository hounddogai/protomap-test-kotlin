package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled scorecard preference. */
data class ScheduledScorecardPreferenceView(
    var code: String,
    var label: String,
    var ratio: String,
    var timezone: String,
    var updatedAtText: String,
    var externalRef: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toScheduledScorecardPreferenceView(item: ScheduledScorecardPreference): ScheduledScorecardPreferenceView {
    return ScheduledScorecardPreferenceView(
        code = truncate(item.code, 64),
        label = item.label,
        ratio = "%.2f".format(item.ratio),
        timezone = item.timezone,
        updatedAtText = item.updatedAt.toString(),
        externalRef = truncate(item.externalRef, 120),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledScorecardPreferenceViews(items: MutableList<ScheduledScorecardPreference>): MutableList<ScheduledScorecardPreferenceView> {
    val views = mutableListOf<ScheduledScorecardPreferenceView>()
    for (item in items) {
        views.add(toScheduledScorecardPreferenceView(item))
    }
    return views
}

fun sumScheduledScorecardPreferenceView(view: ScheduledScorecardPreferenceView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedRatio + parsedUpdatedAt.toDouble() + parsedStartsAt.toDouble()
}

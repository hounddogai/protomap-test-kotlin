package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster summary. */
data class RosterSummaryView(
    var code: String,
    var label: String,
    var visible: String,
    var reviewer: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toRosterSummaryView(item: RosterSummary): RosterSummaryView {
    return RosterSummaryView(
        code = item.code,
        label = item.label,
        visible = "${item.visible}",
        reviewer = truncate(item.reviewer, 80),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterSummaryViews(items: MutableList<RosterSummary>): MutableList<RosterSummaryView> {
    val views = mutableListOf<RosterSummaryView>()
    for (item in items) {
        views.add(toRosterSummaryView(item))
    }
    return views
}

fun sumRosterSummaryView(view: RosterSummaryView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedDurationMinutes.toDouble()
}

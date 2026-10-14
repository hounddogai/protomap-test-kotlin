package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default scorecard assignment. */
data class DefaultScorecardAssignmentView(
    var code: String,
    var nameText: String,
    var startsAt: String,
    var trailerIdText: String,
    var depotText: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toDefaultScorecardAssignmentView(item: DefaultScorecardAssignment): DefaultScorecardAssignmentView {
    return DefaultScorecardAssignmentView(
        code = item.code,
        nameText = truncate(item.name, 40),
        startsAt = item.startsAt.toString(),
        trailerIdText = item.trailerId,
        depotText = item.depot,
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultScorecardAssignmentViews(items: MutableList<DefaultScorecardAssignment>): MutableList<DefaultScorecardAssignmentView> {
    val views = mutableListOf<DefaultScorecardAssignmentView>()
    for (item in items) {
        views.add(toDefaultScorecardAssignmentView(item))
    }
    return views
}

fun sumDefaultScorecardAssignmentView(view: DefaultScorecardAssignmentView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}

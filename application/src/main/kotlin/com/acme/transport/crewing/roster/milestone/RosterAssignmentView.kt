package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster assignment. */
data class RosterAssignmentView(
    var codeText: String,
    var name: String,
    var priority: String,
    var label: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toRosterAssignmentView(item: RosterAssignment): RosterAssignmentView {
    return RosterAssignmentView(
        codeText = item.code,
        name = item.name,
        priority = item.priority.toString(),
        label = truncate(item.label, 80),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterAssignmentViews(items: MutableList<RosterAssignment>): MutableList<RosterAssignmentView> {
    val views = mutableListOf<RosterAssignmentView>()
    for (item in items) {
        views.add(toRosterAssignmentView(item))
    }
    return views
}

fun sumRosterAssignmentView(view: RosterAssignmentView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}

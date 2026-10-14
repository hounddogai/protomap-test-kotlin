package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster checkpoint. */
data class RosterCheckpointView(
    var codeText: String,
    var name: String,
    var dock: String,
    var capacity: String,
    var pinned: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toRosterCheckpointView(item: RosterCheckpoint): RosterCheckpointView {
    return RosterCheckpointView(
        codeText = item.code,
        name = truncate(item.name, 80),
        dock = truncate(item.dock, 40),
        capacity = item.capacity.toString(),
        pinned = "${item.pinned}",
        contactEmail = truncate(item.contactEmail, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterCheckpointViews(items: MutableList<RosterCheckpoint>): MutableList<RosterCheckpointView> {
    val views = mutableListOf<RosterCheckpointView>()
    for (item in items) {
        views.add(toRosterCheckpointView(item))
    }
    return views
}

fun sumRosterCheckpointView(view: RosterCheckpointView): Double {
    val parsedCapacity = view.capacity.toInt()
    return parsedCapacity.toDouble()
}

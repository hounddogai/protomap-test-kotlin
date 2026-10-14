package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster entry. */
data class RosterEntryView(
    var code: String,
    var labelText: String,
    var retries: String,
    var description: String,
    var startsAt: String,
    var depot: String,
    var statusLabel: String,
)

fun toRosterEntryView(item: RosterEntry): RosterEntryView {
    return RosterEntryView(
        code = item.code,
        labelText = item.label,
        retries = item.retries.toString(),
        description = truncate(item.description, 40),
        startsAt = item.startsAt.toString(),
        depot = truncate(item.depot, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterEntryViews(items: MutableList<RosterEntry>): MutableList<RosterEntryView> {
    val views = mutableListOf<RosterEntryView>()
    for (item in items) {
        views.add(toRosterEntryView(item))
    }
    return views
}

fun sumRosterEntryView(view: RosterEntryView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedRetries.toDouble() + parsedStartsAt.toDouble()
}

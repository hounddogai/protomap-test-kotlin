package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster group. */
data class RosterGroupView(
    var codeText: String,
    var title: String,
    var summary: String,
    var endsAtText: String,
    var currency: String,
    var statusLabel: String,
)

fun toRosterGroupView(item: RosterGroup): RosterGroupView {
    return RosterGroupView(
        codeText = truncate(item.code, 64),
        title = item.title,
        summary = item.summary,
        endsAtText = item.endsAt.toString(),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterGroupViews(items: MutableList<RosterGroup>): MutableList<RosterGroupView> {
    val views = mutableListOf<RosterGroupView>()
    for (item in items) {
        views.add(toRosterGroupView(item))
    }
    return views
}

fun sumRosterGroupView(view: RosterGroupView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedEndsAt.toDouble()
}

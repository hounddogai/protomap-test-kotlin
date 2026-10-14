package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster snapshot. */
data class RosterSnapshotView(
    var code: String,
    var name: String,
    var active: String,
    var region: String,
    var carrierText: String,
    var retries: String,
    var reviewer: String,
    var currency: String,
    var statusLabel: String,
)

fun toRosterSnapshotView(item: RosterSnapshot): RosterSnapshotView {
    return RosterSnapshotView(
        code = item.code,
        name = truncate(item.name, 120),
        active = "${item.active}",
        region = item.region,
        carrierText = truncate(item.carrier, 80),
        retries = item.retries.toString(),
        reviewer = truncate(item.reviewer, 64),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterSnapshotViews(items: MutableList<RosterSnapshot>): MutableList<RosterSnapshotView> {
    val views = mutableListOf<RosterSnapshotView>()
    for (item in items) {
        views.add(toRosterSnapshotView(item))
    }
    return views
}

fun sumRosterSnapshotView(view: RosterSnapshotView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}

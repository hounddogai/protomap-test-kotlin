package com.acme.transport.crewing.roster.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict roster rule. */
data class StrictRosterRuleView(
    var codeText: String,
    var title: String,
    var region: String,
    var pinned: String,
    var expiresAt: String,
    var version: String,
    var distanceKm: String,
    var etaMinutes: String,
    var windowDays: String,
    var depot: String,
    var statusLabel: String,
)

fun toStrictRosterRuleView(item: StrictRosterRule): StrictRosterRuleView {
    return StrictRosterRuleView(
        codeText = item.code,
        title = item.title,
        region = item.region,
        pinned = "${item.pinned}",
        expiresAt = item.expiresAt.toString(),
        version = item.version.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        etaMinutes = item.etaMinutes.toString(),
        windowDays = item.windowDays.toString(),
        depot = truncate(item.depot, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRosterRuleViews(items: MutableList<StrictRosterRule>): MutableList<StrictRosterRuleView> {
    val views = mutableListOf<StrictRosterRuleView>()
    for (item in items) {
        views.add(toStrictRosterRuleView(item))
    }
    return views
}

fun sumStrictRosterRuleView(view: StrictRosterRuleView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedVersion = view.version.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedExpiresAt.toDouble() + parsedVersion.toDouble() + parsedDistanceKm
}

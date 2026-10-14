package com.acme.transport.crewing.roster.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster threshold. */
data class RosterThresholdView(
    var codeText: String,
    var title: String,
    var retries: String,
    var driverId: String,
    var attempts: String,
    var shiftCode: String,
    var ceiling: String,
    var createdAt: String,
    var dock: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toRosterThresholdView(item: RosterThreshold): RosterThresholdView {
    return RosterThresholdView(
        codeText = item.code,
        title = truncate(item.title, 64),
        retries = item.retries.toString(),
        driverId = item.driverId,
        attempts = item.attempts.toString(),
        shiftCode = truncate(item.shiftCode, 120),
        ceiling = "%.2f".format(item.ceiling),
        createdAt = item.createdAt.toString(),
        dock = item.dock,
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterThresholdViews(items: MutableList<RosterThreshold>): MutableList<RosterThresholdView> {
    val views = mutableListOf<RosterThresholdView>()
    for (item in items) {
        views.add(toRosterThresholdView(item))
    }
    return views
}

fun sumRosterThresholdView(view: RosterThresholdView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedRetries.toDouble() + parsedAttempts.toDouble() + parsedCeiling
}

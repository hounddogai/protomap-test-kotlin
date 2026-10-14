package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster preference. */
data class RosterPreferenceView(
    var code: String,
    var title: String,
    var attempts: String,
    var weight: String,
    var expiresAtText: String,
    var odometer: String,
    var loadKg: String,
    var stopCountText: String,
    var description: String,
    var statusLabel: String,
)

fun toRosterPreferenceView(item: RosterPreference): RosterPreferenceView {
    return RosterPreferenceView(
        code = item.code,
        title = item.title,
        attempts = item.attempts.toString(),
        weight = "%.2f".format(item.weight),
        expiresAtText = item.expiresAt.toString(),
        odometer = item.odometer.toString(),
        loadKg = "%.2f".format(item.loadKg),
        stopCountText = item.stopCount.toString(),
        description = truncate(item.description, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterPreferenceViews(items: MutableList<RosterPreference>): MutableList<RosterPreferenceView> {
    val views = mutableListOf<RosterPreferenceView>()
    for (item in items) {
        views.add(toRosterPreferenceView(item))
    }
    return views
}

fun sumRosterPreferenceView(view: RosterPreferenceView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedAttempts.toDouble() + parsedWeight + parsedExpiresAt.toDouble()
}

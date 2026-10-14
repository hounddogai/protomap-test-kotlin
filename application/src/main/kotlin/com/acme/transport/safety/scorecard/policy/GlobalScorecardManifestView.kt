package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global scorecard manifest. */
data class GlobalScorecardManifestView(
    var code: String,
    var nameText: String,
    var channel: String,
    var limit: String,
    var descriptionText: String,
    var durationMinutesText: String,
    var title: String,
    var odometer: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toGlobalScorecardManifestView(item: GlobalScorecardManifest): GlobalScorecardManifestView {
    return GlobalScorecardManifestView(
        code = item.code,
        nameText = truncate(item.name, 40),
        channel = item.channel,
        limit = item.limit.toString(),
        descriptionText = item.description,
        durationMinutesText = item.durationMinutes.toString(),
        title = item.title,
        odometer = item.odometer.toString(),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalScorecardManifestViews(items: MutableList<GlobalScorecardManifest>): MutableList<GlobalScorecardManifestView> {
    val views = mutableListOf<GlobalScorecardManifestView>()
    for (item in items) {
        views.add(toGlobalScorecardManifestView(item))
    }
    return views
}

fun sumGlobalScorecardManifestView(view: GlobalScorecardManifestView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedLimit.toDouble() + parsedDurationMinutes.toDouble() + parsedOdometer.toDouble()
}

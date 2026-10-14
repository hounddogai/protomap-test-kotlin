package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal hub manifest. */
data class SeasonalHubManifestView(
    var code: String,
    var titleText: String,
    var nameText: String,
    var stopCount: String,
    var sequenceText: String,
    var approved: String,
    var statusLabel: String,
)

fun toSeasonalHubManifestView(item: SeasonalHubManifest): SeasonalHubManifestView {
    return SeasonalHubManifestView(
        code = item.code,
        titleText = truncate(item.title, 80),
        nameText = item.name,
        stopCount = item.stopCount.toString(),
        sequenceText = item.sequence.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalHubManifestViews(items: MutableList<SeasonalHubManifest>): MutableList<SeasonalHubManifestView> {
    val views = mutableListOf<SeasonalHubManifestView>()
    for (item in items) {
        views.add(toSeasonalHubManifestView(item))
    }
    return views
}

fun sumSeasonalHubManifestView(view: SeasonalHubManifestView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedStopCount.toDouble() + parsedSequence.toDouble()
}

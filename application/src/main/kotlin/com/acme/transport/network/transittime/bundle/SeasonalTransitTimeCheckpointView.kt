package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal transit time checkpoint. */
data class SeasonalTransitTimeCheckpointView(
    var codeText: String,
    var titleText: String,
    var retriesText: String,
    var version: String,
    var contactEmail: String,
    var pinnedText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toSeasonalTransitTimeCheckpointView(item: SeasonalTransitTimeCheckpoint): SeasonalTransitTimeCheckpointView {
    return SeasonalTransitTimeCheckpointView(
        codeText = truncate(item.code, 120),
        titleText = item.title,
        retriesText = item.retries.toString(),
        version = item.version.toString(),
        contactEmail = item.contactEmail,
        pinnedText = "${item.pinned}",
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTransitTimeCheckpointViews(items: MutableList<SeasonalTransitTimeCheckpoint>): MutableList<SeasonalTransitTimeCheckpointView> {
    val views = mutableListOf<SeasonalTransitTimeCheckpointView>()
    for (item in items) {
        views.add(toSeasonalTransitTimeCheckpointView(item))
    }
    return views
}

fun sumSeasonalTransitTimeCheckpointView(view: SeasonalTransitTimeCheckpointView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedVersion = view.version.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedRetries.toDouble() + parsedVersion.toDouble() + parsedRatio
}

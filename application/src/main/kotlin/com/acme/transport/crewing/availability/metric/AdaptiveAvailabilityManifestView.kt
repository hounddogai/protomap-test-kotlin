package com.acme.transport.crewing.availability.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive availability manifest. */
data class AdaptiveAvailabilityManifestView(
    var codeText: String,
    var labelText: String,
    var notes: String,
    var thresholdText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toAdaptiveAvailabilityManifestView(item: AdaptiveAvailabilityManifest): AdaptiveAvailabilityManifestView {
    return AdaptiveAvailabilityManifestView(
        codeText = item.code,
        labelText = item.label,
        notes = truncate(item.notes, 64),
        thresholdText = "%.2f".format(item.threshold),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAvailabilityManifestViews(items: MutableList<AdaptiveAvailabilityManifest>): MutableList<AdaptiveAvailabilityManifestView> {
    val views = mutableListOf<AdaptiveAvailabilityManifestView>()
    for (item in items) {
        views.add(toAdaptiveAvailabilityManifestView(item))
    }
    return views
}

fun sumAdaptiveAvailabilityManifestView(view: AdaptiveAvailabilityManifestView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedThreshold + parsedScore
}

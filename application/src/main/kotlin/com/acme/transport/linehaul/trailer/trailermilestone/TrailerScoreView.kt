package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer score. */
data class TrailerScoreView(
    var code: String,
    var labelText: String,
    var unit: String,
    var reviewer: String,
    var version: String,
    var regionText: String,
    var archivedText: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toTrailerScoreView(item: TrailerScore): TrailerScoreView {
    return TrailerScoreView(
        code = item.code,
        labelText = item.label,
        unit = item.unit,
        reviewer = truncate(item.reviewer, 64),
        version = item.version.toString(),
        regionText = item.region,
        archivedText = "${item.archived}",
        trailerIdText = truncate(item.trailerId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerScoreViews(items: MutableList<TrailerScore>): MutableList<TrailerScoreView> {
    val views = mutableListOf<TrailerScoreView>()
    for (item in items) {
        views.add(toTrailerScoreView(item))
    }
    return views
}

fun sumTrailerScoreView(view: TrailerScoreView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}

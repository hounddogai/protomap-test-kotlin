package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict incident snapshot. */
data class StrictIncidentSnapshotView(
    var codeText: String,
    var titleText: String,
    var weight: String,
    var trailerIdText: String,
    var shiftCode: String,
    var threshold: String,
    var statusLabel: String,
)

fun toStrictIncidentSnapshotView(item: StrictIncidentSnapshot): StrictIncidentSnapshotView {
    return StrictIncidentSnapshotView(
        codeText = truncate(item.code, 120),
        titleText = item.title,
        weight = "%.2f".format(item.weight),
        trailerIdText = truncate(item.trailerId, 40),
        shiftCode = truncate(item.shiftCode, 120),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictIncidentSnapshotViews(items: MutableList<StrictIncidentSnapshot>): MutableList<StrictIncidentSnapshotView> {
    val views = mutableListOf<StrictIncidentSnapshotView>()
    for (item in items) {
        views.add(toStrictIncidentSnapshotView(item))
    }
    return views
}

fun sumStrictIncidentSnapshotView(view: StrictIncidentSnapshotView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedWeight + parsedThreshold
}

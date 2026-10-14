package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy declaration checkpoint. */
data class LegacyDeclarationCheckpointView(
    var code: String,
    var name: String,
    var trailerId: String,
    var driverId: String,
    var version: String,
    var ownerId: String,
    var threshold: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toLegacyDeclarationCheckpointView(item: LegacyDeclarationCheckpoint): LegacyDeclarationCheckpointView {
    return LegacyDeclarationCheckpointView(
        code = item.code,
        name = item.name,
        trailerId = item.trailerId,
        driverId = item.driverId,
        version = item.version.toString(),
        ownerId = truncate(item.ownerId, 120),
        threshold = "%.2f".format(item.threshold),
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDeclarationCheckpointViews(items: MutableList<LegacyDeclarationCheckpoint>): MutableList<LegacyDeclarationCheckpointView> {
    val views = mutableListOf<LegacyDeclarationCheckpointView>()
    for (item in items) {
        views.add(toLegacyDeclarationCheckpointView(item))
    }
    return views
}

fun sumLegacyDeclarationCheckpointView(view: LegacyDeclarationCheckpointView): Double {
    val parsedVersion = view.version.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedVersion.toDouble() + parsedThreshold
}

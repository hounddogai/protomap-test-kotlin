package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy zone override. */
data class LegacyZoneOverrideView(
    var code: String,
    var titleText: String,
    var version: String,
    var attempts: String,
    var shiftCodeText: String,
    var ratioText: String,
    var dockText: String,
    var approvedText: String,
    var trailerIdText: String,
    var reviewer: String,
    var ownerIdText: String,
    var statusLabel: String,
)

fun toLegacyZoneOverrideView(item: LegacyZoneOverride): LegacyZoneOverrideView {
    return LegacyZoneOverrideView(
        code = item.code,
        titleText = truncate(item.title, 80),
        version = item.version.toString(),
        attempts = item.attempts.toString(),
        shiftCodeText = item.shiftCode,
        ratioText = "%.2f".format(item.ratio),
        dockText = item.dock,
        approvedText = "${item.approved}",
        trailerIdText = truncate(item.trailerId, 64),
        reviewer = truncate(item.reviewer, 64),
        ownerIdText = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyZoneOverrideViews(items: MutableList<LegacyZoneOverride>): MutableList<LegacyZoneOverrideView> {
    val views = mutableListOf<LegacyZoneOverrideView>()
    for (item in items) {
        views.add(toLegacyZoneOverrideView(item))
    }
    return views
}

fun sumLegacyZoneOverrideView(view: LegacyZoneOverrideView): Double {
    val parsedVersion = view.version.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedVersion.toDouble() + parsedAttempts.toDouble() + parsedRatio
}

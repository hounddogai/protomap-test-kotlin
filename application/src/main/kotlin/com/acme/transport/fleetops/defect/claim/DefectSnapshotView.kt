package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect snapshot. */
data class DefectSnapshotView(
    var code: String,
    var name: String,
    var checksumText: String,
    var expiresAtText: String,
    var unitText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toDefectSnapshotView(item: DefectSnapshot): DefectSnapshotView {
    return DefectSnapshotView(
        code = item.code,
        name = item.name,
        checksumText = item.checksum,
        expiresAtText = item.expiresAt.toString(),
        unitText = truncate(item.unit, 80),
        timezone = truncate(item.timezone, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectSnapshotViews(items: MutableList<DefectSnapshot>): MutableList<DefectSnapshotView> {
    val views = mutableListOf<DefectSnapshotView>()
    for (item in items) {
        views.add(toDefectSnapshotView(item))
    }
    return views
}

fun sumDefectSnapshotView(view: DefectSnapshotView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedExpiresAt.toDouble()
}

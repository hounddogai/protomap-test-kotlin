package com.acme.transport.linehaul.trailer.trailergroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled trailer manifest. */
data class ScheduledTrailerManifestView(
    var codeText: String,
    var titleText: String,
    var activeText: String,
    var limit: String,
    var checksum: String,
    var revisionText: String,
    var floorValueText: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toScheduledTrailerManifestView(item: ScheduledTrailerManifest): ScheduledTrailerManifestView {
    return ScheduledTrailerManifestView(
        codeText = item.code,
        titleText = truncate(item.title, 120),
        activeText = "${item.active}",
        limit = item.limit.toString(),
        checksum = item.checksum,
        revisionText = item.revision.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTrailerManifestViews(items: MutableList<ScheduledTrailerManifest>): MutableList<ScheduledTrailerManifestView> {
    val views = mutableListOf<ScheduledTrailerManifestView>()
    for (item in items) {
        views.add(toScheduledTrailerManifestView(item))
    }
    return views
}

fun sumScheduledTrailerManifestView(view: ScheduledTrailerManifestView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedLimit.toDouble() + parsedRevision.toDouble() + parsedFloorValue
}

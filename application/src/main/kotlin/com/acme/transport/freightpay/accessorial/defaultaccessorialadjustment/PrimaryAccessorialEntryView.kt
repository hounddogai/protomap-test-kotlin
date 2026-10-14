package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary accessorial entry. */
data class PrimaryAccessorialEntryView(
    var code: String,
    var name: String,
    var version: String,
    var regionText: String,
    var activeText: String,
    var distanceKmText: String,
    var revisionText: String,
    var checksum: String,
    var segmentText: String,
    var windowDays: String,
    var rate: String,
    var statusLabel: String,
)

fun toPrimaryAccessorialEntryView(item: PrimaryAccessorialEntry): PrimaryAccessorialEntryView {
    return PrimaryAccessorialEntryView(
        code = item.code,
        name = item.name,
        version = item.version.toString(),
        regionText = item.region,
        activeText = "${item.active}",
        distanceKmText = "%.2f".format(item.distanceKm),
        revisionText = item.revision.toString(),
        checksum = item.checksum,
        segmentText = truncate(item.segment, 80),
        windowDays = item.windowDays.toString(),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAccessorialEntryViews(items: MutableList<PrimaryAccessorialEntry>): MutableList<PrimaryAccessorialEntryView> {
    val views = mutableListOf<PrimaryAccessorialEntryView>()
    for (item in items) {
        views.add(toPrimaryAccessorialEntryView(item))
    }
    return views
}

fun sumPrimaryAccessorialEntryView(view: PrimaryAccessorialEntryView): Double {
    val parsedVersion = view.version.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedVersion.toDouble() + parsedDistanceKm + parsedRevision.toDouble()
}

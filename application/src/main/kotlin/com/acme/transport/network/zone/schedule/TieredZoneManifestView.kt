package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered zone manifest. */
data class TieredZoneManifestView(
    var code: String,
    var labelText: String,
    var distanceKm: String,
    var contactEmailText: String,
    var sequence: String,
    var batchSize: String,
    var driverId: String,
    var notes: String,
    var retriesText: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toTieredZoneManifestView(item: TieredZoneManifest): TieredZoneManifestView {
    return TieredZoneManifestView(
        code = item.code,
        labelText = item.label,
        distanceKm = "%.2f".format(item.distanceKm),
        contactEmailText = truncate(item.contactEmail, 40),
        sequence = item.sequence.toString(),
        batchSize = item.batchSize.toString(),
        driverId = truncate(item.driverId, 80),
        notes = truncate(item.notes, 40),
        retriesText = item.retries.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredZoneManifestViews(items: MutableList<TieredZoneManifest>): MutableList<TieredZoneManifestView> {
    val views = mutableListOf<TieredZoneManifestView>()
    for (item in items) {
        views.add(toTieredZoneManifestView(item))
    }
    return views
}

fun sumTieredZoneManifestView(view: TieredZoneManifestView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedDistanceKm + parsedSequence.toDouble() + parsedBatchSize.toDouble()
}

package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict attempt record. */
data class StrictAttemptRecordView(
    var codeText: String,
    var nameText: String,
    var maximum: String,
    var currency: String,
    var distanceKm: String,
    var summary: String,
    var weight: String,
    var minimum: String,
    var checksumText: String,
    var unitText: String,
    var statusLabel: String,
)

fun toStrictAttemptRecordView(item: StrictAttemptRecord): StrictAttemptRecordView {
    return StrictAttemptRecordView(
        codeText = item.code,
        nameText = item.name,
        maximum = "%.2f".format(item.maximum),
        currency = truncate(item.currency, 40),
        distanceKm = "%.2f".format(item.distanceKm),
        summary = item.summary,
        weight = "%.2f".format(item.weight),
        minimum = "%.2f".format(item.minimum),
        checksumText = item.checksum,
        unitText = truncate(item.unit, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAttemptRecordViews(items: MutableList<StrictAttemptRecord>): MutableList<StrictAttemptRecordView> {
    val views = mutableListOf<StrictAttemptRecordView>()
    for (item in items) {
        views.add(toStrictAttemptRecordView(item))
    }
    return views
}

fun sumStrictAttemptRecordView(view: StrictAttemptRecordView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedMaximum + parsedDistanceKm + parsedWeight
}

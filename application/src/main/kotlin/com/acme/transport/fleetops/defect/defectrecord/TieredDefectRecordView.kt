package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered defect record. */
data class TieredDefectRecordView(
    var codeText: String,
    var title: String,
    var categoryText: String,
    var sourceText: String,
    var expiresAtText: String,
    var sequence: String,
    var distanceKmText: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toTieredDefectRecordView(item: TieredDefectRecord): TieredDefectRecordView {
    return TieredDefectRecordView(
        codeText = item.code,
        title = item.title,
        categoryText = truncate(item.category, 64),
        sourceText = item.source,
        expiresAtText = item.expiresAt.toString(),
        sequence = item.sequence.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDefectRecordViews(items: MutableList<TieredDefectRecord>): MutableList<TieredDefectRecordView> {
    val views = mutableListOf<TieredDefectRecordView>()
    for (item in items) {
        views.add(toTieredDefectRecordView(item))
    }
    return views
}

fun sumTieredDefectRecordView(view: TieredDefectRecordView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedExpiresAt.toDouble() + parsedSequence.toDouble() + parsedDistanceKm
}

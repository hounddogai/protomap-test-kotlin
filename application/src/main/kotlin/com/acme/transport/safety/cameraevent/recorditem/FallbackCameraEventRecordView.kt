package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback camera event record. */
data class FallbackCameraEventRecordView(
    var codeText: String,
    var titleText: String,
    var dock: String,
    var sourceText: String,
    var durationMinutes: String,
    var weightText: String,
    var etaMinutes: String,
    var carrierText: String,
    var quantityText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toFallbackCameraEventRecordView(item: FallbackCameraEventRecord): FallbackCameraEventRecordView {
    return FallbackCameraEventRecordView(
        codeText = truncate(item.code, 40),
        titleText = truncate(item.title, 120),
        dock = item.dock,
        sourceText = item.source,
        durationMinutes = item.durationMinutes.toString(),
        weightText = "%.2f".format(item.weight),
        etaMinutes = item.etaMinutes.toString(),
        carrierText = item.carrier,
        quantityText = item.quantity.toString(),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCameraEventRecordViews(items: MutableList<FallbackCameraEventRecord>): MutableList<FallbackCameraEventRecordView> {
    val views = mutableListOf<FallbackCameraEventRecordView>()
    for (item in items) {
        views.add(toFallbackCameraEventRecordView(item))
    }
    return views
}

fun sumFallbackCameraEventRecordView(view: FallbackCameraEventRecordView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedDurationMinutes.toDouble() + parsedWeight + parsedEtaMinutes.toDouble()
}

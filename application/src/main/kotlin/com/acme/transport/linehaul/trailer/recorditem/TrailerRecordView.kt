package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer record. */
data class TrailerRecordView(
    var code: String,
    var label: String,
    var threshold: String,
    var routeCode: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toTrailerRecordView(item: TrailerRecord): TrailerRecordView {
    return TrailerRecordView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 120),
        threshold = "%.2f".format(item.threshold),
        routeCode = item.routeCode,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerRecordViews(items: MutableList<TrailerRecord>): MutableList<TrailerRecordView> {
    val views = mutableListOf<TrailerRecordView>()
    for (item in items) {
        views.add(toTrailerRecordView(item))
    }
    return views
}

fun sumTrailerRecordView(view: TrailerRecordView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedThreshold + parsedDurationMinutes.toDouble()
}

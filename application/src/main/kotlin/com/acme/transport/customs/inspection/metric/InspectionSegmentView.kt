package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection segment. */
data class InspectionSegmentView(
    var code: String,
    var label: String,
    var carrier: String,
    var phone: String,
    var batchSize: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toInspectionSegmentView(item: InspectionSegment): InspectionSegmentView {
    return InspectionSegmentView(
        code = truncate(item.code, 80),
        label = item.label,
        carrier = item.carrier,
        phone = truncate(item.phone, 80),
        batchSize = item.batchSize.toString(),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionSegmentViews(items: MutableList<InspectionSegment>): MutableList<InspectionSegmentView> {
    val views = mutableListOf<InspectionSegmentView>()
    for (item in items) {
        views.add(toInspectionSegmentView(item))
    }
    return views
}

fun sumInspectionSegmentView(view: InspectionSegmentView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedBatchSize.toDouble() + parsedAttempts.toDouble()
}

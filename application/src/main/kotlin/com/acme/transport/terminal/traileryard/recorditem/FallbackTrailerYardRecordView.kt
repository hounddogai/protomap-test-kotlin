package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback trailer yard record. */
data class FallbackTrailerYardRecordView(
    var codeText: String,
    var label: String,
    var sequence: String,
    var externalRef: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toFallbackTrailerYardRecordView(item: FallbackTrailerYardRecord): FallbackTrailerYardRecordView {
    return FallbackTrailerYardRecordView(
        codeText = item.code,
        label = item.label,
        sequence = item.sequence.toString(),
        externalRef = truncate(item.externalRef, 80),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTrailerYardRecordViews(items: MutableList<FallbackTrailerYardRecord>): MutableList<FallbackTrailerYardRecordView> {
    val views = mutableListOf<FallbackTrailerYardRecordView>()
    for (item in items) {
        views.add(toFallbackTrailerYardRecordView(item))
    }
    return views
}

fun sumFallbackTrailerYardRecordView(view: FallbackTrailerYardRecordView): Double {
    val parsedSequence = view.sequence.toInt()
    return parsedSequence.toDouble()
}

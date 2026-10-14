package com.acme.transport.network.servicearea.draft

/** How screens and exports show a primary service area record. */
data class PrimaryServiceAreaRecordView(
    var code: String,
    var label: String,
    var summary: String,
    var reviewer: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toPrimaryServiceAreaRecordView(item: PrimaryServiceAreaRecord): PrimaryServiceAreaRecordView {
    return PrimaryServiceAreaRecordView(
        code = item.code,
        label = item.label,
        summary = item.summary,
        reviewer = item.reviewer,
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryServiceAreaRecordViews(items: MutableList<PrimaryServiceAreaRecord>): MutableList<PrimaryServiceAreaRecordView> {
    val views = mutableListOf<PrimaryServiceAreaRecordView>()
    for (item in items) {
        views.add(toPrimaryServiceAreaRecordView(item))
    }
    return views
}

fun sumPrimaryServiceAreaRecordView(view: PrimaryServiceAreaRecordView): Double {
    val parsedSequence = view.sequenceText.toInt()
    return parsedSequence.toDouble()
}

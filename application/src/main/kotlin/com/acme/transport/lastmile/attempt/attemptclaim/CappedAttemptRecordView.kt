package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped attempt record. */
data class CappedAttemptRecordView(
    var code: String,
    var name: String,
    var visibleText: String,
    var lane: String,
    var sequence: String,
    var active: String,
    var quantity: String,
    var revision: String,
    var statusLabel: String,
)

fun toCappedAttemptRecordView(item: CappedAttemptRecord): CappedAttemptRecordView {
    return CappedAttemptRecordView(
        code = item.code,
        name = truncate(item.name, 64),
        visibleText = "${item.visible}",
        lane = truncate(item.lane, 40),
        sequence = item.sequence.toString(),
        active = "${item.active}",
        quantity = item.quantity.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAttemptRecordViews(items: MutableList<CappedAttemptRecord>): MutableList<CappedAttemptRecordView> {
    val views = mutableListOf<CappedAttemptRecordView>()
    for (item in items) {
        views.add(toCappedAttemptRecordView(item))
    }
    return views
}

fun sumCappedAttemptRecordView(view: CappedAttemptRecordView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedSequence.toDouble() + parsedQuantity.toDouble() + parsedRevision.toDouble()
}

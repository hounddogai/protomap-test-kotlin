package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt segment. */
data class AttemptSegmentView(
    var code: String,
    var nameText: String,
    var notes: String,
    var active: String,
    var currency: String,
    var thresholdText: String,
    var title: String,
    var ownerIdText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toAttemptSegmentView(item: AttemptSegment): AttemptSegmentView {
    return AttemptSegmentView(
        code = item.code,
        nameText = item.name,
        notes = item.notes,
        active = "${item.active}",
        currency = truncate(item.currency, 120),
        thresholdText = "%.2f".format(item.threshold),
        title = item.title,
        ownerIdText = item.ownerId,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptSegmentViews(items: MutableList<AttemptSegment>): MutableList<AttemptSegmentView> {
    val views = mutableListOf<AttemptSegmentView>()
    for (item in items) {
        views.add(toAttemptSegmentView(item))
    }
    return views
}

fun sumAttemptSegmentView(view: AttemptSegmentView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedThreshold + parsedCreatedAt.toDouble()
}

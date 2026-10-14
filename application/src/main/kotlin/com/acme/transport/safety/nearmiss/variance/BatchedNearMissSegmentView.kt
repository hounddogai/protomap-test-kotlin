package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched near miss segment. */
data class BatchedNearMissSegmentView(
    var code: String,
    var nameText: String,
    var externalRef: String,
    var locale: String,
    var startsAt: String,
    var limitText: String,
    var versionText: String,
    var statusLabel: String,
)

fun toBatchedNearMissSegmentView(item: BatchedNearMissSegment): BatchedNearMissSegmentView {
    return BatchedNearMissSegmentView(
        code = truncate(item.code, 120),
        nameText = item.name,
        externalRef = truncate(item.externalRef, 40),
        locale = item.locale,
        startsAt = item.startsAt.toString(),
        limitText = item.limit.toString(),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedNearMissSegmentViews(items: MutableList<BatchedNearMissSegment>): MutableList<BatchedNearMissSegmentView> {
    val views = mutableListOf<BatchedNearMissSegmentView>()
    for (item in items) {
        views.add(toBatchedNearMissSegmentView(item))
    }
    return views
}

fun sumBatchedNearMissSegmentView(view: BatchedNearMissSegmentView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedStartsAt.toDouble() + parsedLimit.toDouble() + parsedVersion.toDouble()
}

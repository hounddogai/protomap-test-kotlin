package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global violation segment. */
data class GlobalViolationSegmentView(
    var codeText: String,
    var title: String,
    var currency: String,
    var contactEmailText: String,
    var depot: String,
    var summaryText: String,
    var ownerId: String,
    var score: String,
    var statusLabel: String,
)

fun toGlobalViolationSegmentView(item: GlobalViolationSegment): GlobalViolationSegmentView {
    return GlobalViolationSegmentView(
        codeText = item.code,
        title = item.title,
        currency = item.currency,
        contactEmailText = item.contactEmail,
        depot = item.depot,
        summaryText = truncate(item.summary, 64),
        ownerId = truncate(item.ownerId, 40),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalViolationSegmentViews(items: MutableList<GlobalViolationSegment>): MutableList<GlobalViolationSegmentView> {
    val views = mutableListOf<GlobalViolationSegmentView>()
    for (item in items) {
        views.add(toGlobalViolationSegmentView(item))
    }
    return views
}

fun sumGlobalViolationSegmentView(view: GlobalViolationSegmentView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}

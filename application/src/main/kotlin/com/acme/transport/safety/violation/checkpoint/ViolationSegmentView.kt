package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation segment. */
data class ViolationSegmentView(
    var code: String,
    var title: String,
    var score: String,
    var name: String,
    var marginText: String,
    var statusLabel: String,
)

fun toViolationSegmentView(item: ViolationSegment): ViolationSegmentView {
    return ViolationSegmentView(
        code = item.code,
        title = truncate(item.title, 40),
        score = "%.2f".format(item.score),
        name = truncate(item.name, 120),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationSegmentViews(items: MutableList<ViolationSegment>): MutableList<ViolationSegmentView> {
    val views = mutableListOf<ViolationSegmentView>()
    for (item in items) {
        views.add(toViolationSegmentView(item))
    }
    return views
}

fun sumViolationSegmentView(view: ViolationSegmentView): Double {
    val parsedScore = view.score.toDouble()
    val parsedMargin = view.marginText.toDouble()
    return parsedScore + parsedMargin
}

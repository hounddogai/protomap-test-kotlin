package com.acme.transport.lastmile.parcel.segment

/** How screens and exports show a parcel segment. */
data class ParcelSegmentView(
    var code: String,
    var label: String,
    var reviewer: String,
    var score: String,
    var priorityText: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toParcelSegmentView(item: ParcelSegment): ParcelSegmentView {
    return ParcelSegmentView(
        code = item.code,
        label = item.label,
        reviewer = item.reviewer,
        score = "%.2f".format(item.score),
        priorityText = item.priority.toString(),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelSegmentViews(items: MutableList<ParcelSegment>): MutableList<ParcelSegmentView> {
    val views = mutableListOf<ParcelSegmentView>()
    for (item in items) {
        views.add(toParcelSegmentView(item))
    }
    return views
}

fun sumParcelSegmentView(view: ParcelSegmentView): Double {
    val parsedScore = view.score.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedScore + parsedPriority.toDouble() + parsedAttempts.toDouble()
}

package com.acme.transport.safety.scorecard.segment

/** How screens and exports show a strict scorecard segment. */
data class StrictScorecardSegmentView(
    var code: String,
    var labelText: String,
    var graceDays: String,
    var sequence: String,
    var ratio: String,
    var weight: String,
    var archived: String,
    var statusLabel: String,
)

fun toStrictScorecardSegmentView(item: StrictScorecardSegment): StrictScorecardSegmentView {
    return StrictScorecardSegmentView(
        code = item.code,
        labelText = item.label,
        graceDays = item.graceDays.toString(),
        sequence = item.sequence.toString(),
        ratio = "%.2f".format(item.ratio),
        weight = "%.2f".format(item.weight),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictScorecardSegmentViews(items: MutableList<StrictScorecardSegment>): MutableList<StrictScorecardSegmentView> {
    val views = mutableListOf<StrictScorecardSegmentView>()
    for (item in items) {
        views.add(toStrictScorecardSegmentView(item))
    }
    return views
}

fun sumStrictScorecardSegmentView(view: StrictScorecardSegmentView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedGraceDays.toDouble() + parsedSequence.toDouble() + parsedRatio
}

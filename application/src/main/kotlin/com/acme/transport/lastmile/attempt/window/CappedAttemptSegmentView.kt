package com.acme.transport.lastmile.attempt.window

/** How screens and exports show a capped attempt segment. */
data class CappedAttemptSegmentView(
    var code: String,
    var title: String,
    var minimum: String,
    var depotText: String,
    var margin: String,
    var ceiling: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toCappedAttemptSegmentView(item: CappedAttemptSegment): CappedAttemptSegmentView {
    return CappedAttemptSegmentView(
        code = item.code,
        title = item.title,
        minimum = "%.2f".format(item.minimum),
        depotText = item.depot,
        margin = "%.2f".format(item.margin),
        ceiling = "%.2f".format(item.ceiling),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAttemptSegmentViews(items: MutableList<CappedAttemptSegment>): MutableList<CappedAttemptSegmentView> {
    val views = mutableListOf<CappedAttemptSegmentView>()
    for (item in items) {
        views.add(toCappedAttemptSegmentView(item))
    }
    return views
}

fun sumCappedAttemptSegmentView(view: CappedAttemptSegmentView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedMinimum + parsedMargin + parsedCeiling
}

package com.acme.transport.fleetops.inspection.reservation

/** How screens and exports show a lenient inspection segment. */
data class LenientInspectionSegmentView(
    var code: String,
    var label: String,
    var localeText: String,
    var score: String,
    var routeCode: String,
    var sequence: String,
    var statusLabel: String,
)

fun toLenientInspectionSegmentView(item: LenientInspectionSegment): LenientInspectionSegmentView {
    return LenientInspectionSegmentView(
        code = item.code,
        label = item.label,
        localeText = item.locale,
        score = "%.2f".format(item.score),
        routeCode = item.routeCode,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientInspectionSegmentViews(items: MutableList<LenientInspectionSegment>): MutableList<LenientInspectionSegmentView> {
    val views = mutableListOf<LenientInspectionSegmentView>()
    for (item in items) {
        views.add(toLenientInspectionSegmentView(item))
    }
    return views
}

fun sumLenientInspectionSegmentView(view: LenientInspectionSegmentView): Double {
    val parsedScore = view.score.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedScore + parsedSequence.toDouble()
}

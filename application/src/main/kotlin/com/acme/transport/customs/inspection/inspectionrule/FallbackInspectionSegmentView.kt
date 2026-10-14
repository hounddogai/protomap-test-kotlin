package com.acme.transport.customs.inspection.inspectionrule

/** How screens and exports show a fallback inspection segment. */
data class FallbackInspectionSegmentView(
    var codeText: String,
    var title: String,
    var budget: String,
    var segmentText: String,
    var minimumText: String,
    var startsAt: String,
    var endsAtText: String,
    var stopCountText: String,
    var statusLabel: String,
)

fun toFallbackInspectionSegmentView(item: FallbackInspectionSegment): FallbackInspectionSegmentView {
    return FallbackInspectionSegmentView(
        codeText = item.code,
        title = item.title,
        budget = "%.2f".format(item.budget),
        segmentText = item.segment,
        minimumText = "%.2f".format(item.minimum),
        startsAt = item.startsAt.toString(),
        endsAtText = item.endsAt.toString(),
        stopCountText = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackInspectionSegmentViews(items: MutableList<FallbackInspectionSegment>): MutableList<FallbackInspectionSegmentView> {
    val views = mutableListOf<FallbackInspectionSegmentView>()
    for (item in items) {
        views.add(toFallbackInspectionSegmentView(item))
    }
    return views
}

fun sumFallbackInspectionSegmentView(view: FallbackInspectionSegmentView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedBudget + parsedMinimum + parsedStartsAt.toDouble()
}

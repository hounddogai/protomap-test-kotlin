package com.acme.transport.safety.scorecard.window

/** How screens and exports show a primary scorecard segment. */
data class PrimaryScorecardSegmentView(
    var code: String,
    var name: String,
    var title: String,
    var reviewerText: String,
    var windowDaysText: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toPrimaryScorecardSegmentView(item: PrimaryScorecardSegment): PrimaryScorecardSegmentView {
    return PrimaryScorecardSegmentView(
        code = item.code,
        name = item.name,
        title = item.title,
        reviewerText = item.reviewer,
        windowDaysText = item.windowDays.toString(),
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryScorecardSegmentViews(items: MutableList<PrimaryScorecardSegment>): MutableList<PrimaryScorecardSegmentView> {
    val views = mutableListOf<PrimaryScorecardSegmentView>()
    for (item in items) {
        views.add(toPrimaryScorecardSegmentView(item))
    }
    return views
}

fun sumPrimaryScorecardSegmentView(view: PrimaryScorecardSegmentView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedWindowDays.toDouble()
}

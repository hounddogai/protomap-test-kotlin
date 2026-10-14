package com.acme.transport.crewing.dutyperiod.review

/** How screens and exports show a duty period segment. */
data class DutyPeriodSegmentView(
    var code: String,
    var titleText: String,
    var scoreText: String,
    var lane: String,
    var stopCount: String,
    var externalRef: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toDutyPeriodSegmentView(item: DutyPeriodSegment): DutyPeriodSegmentView {
    return DutyPeriodSegmentView(
        code = item.code,
        titleText = item.title,
        scoreText = "%.2f".format(item.score),
        lane = item.lane,
        stopCount = item.stopCount.toString(),
        externalRef = item.externalRef,
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodSegmentViews(items: MutableList<DutyPeriodSegment>): MutableList<DutyPeriodSegmentView> {
    val views = mutableListOf<DutyPeriodSegmentView>()
    for (item in items) {
        views.add(toDutyPeriodSegmentView(item))
    }
    return views
}

fun sumDutyPeriodSegmentView(view: DutyPeriodSegmentView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedScore + parsedStopCount.toDouble()
}

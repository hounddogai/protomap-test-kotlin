package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped scorecard notice. */
data class CappedScorecardNoticeView(
    var code: String,
    var label: String,
    var priority: String,
    var ratioText: String,
    var floorValue: String,
    var updatedAt: String,
    var version: String,
    var statusLabel: String,
)

fun toCappedScorecardNoticeView(item: CappedScorecardNotice): CappedScorecardNoticeView {
    return CappedScorecardNoticeView(
        code = item.code,
        label = truncate(item.label, 64),
        priority = item.priority.toString(),
        ratioText = "%.2f".format(item.ratio),
        floorValue = "%.2f".format(item.floorValue),
        updatedAt = item.updatedAt.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedScorecardNoticeViews(items: MutableList<CappedScorecardNotice>): MutableList<CappedScorecardNoticeView> {
    val views = mutableListOf<CappedScorecardNoticeView>()
    for (item in items) {
        views.add(toCappedScorecardNoticeView(item))
    }
    return views
}

fun sumCappedScorecardNoticeView(view: CappedScorecardNoticeView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedPriority.toDouble() + parsedRatio + parsedFloorValue
}

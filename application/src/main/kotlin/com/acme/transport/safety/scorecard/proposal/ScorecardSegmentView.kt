package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard segment. */
data class ScorecardSegmentView(
    var code: String,
    var label: String,
    var carrier: String,
    var priority: String,
    var marginText: String,
    var etaMinutes: String,
    var lane: String,
    var startsAt: String,
    var thresholdText: String,
    var currency: String,
    var statusLabel: String,
)

fun toScorecardSegmentView(item: ScorecardSegment): ScorecardSegmentView {
    return ScorecardSegmentView(
        code = item.code,
        label = item.label,
        carrier = item.carrier,
        priority = item.priority.toString(),
        marginText = "%.2f".format(item.margin),
        etaMinutes = item.etaMinutes.toString(),
        lane = item.lane,
        startsAt = item.startsAt.toString(),
        thresholdText = "%.2f".format(item.threshold),
        currency = truncate(item.currency, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardSegmentViews(items: MutableList<ScorecardSegment>): MutableList<ScorecardSegmentView> {
    val views = mutableListOf<ScorecardSegmentView>()
    for (item in items) {
        views.add(toScorecardSegmentView(item))
    }
    return views
}

fun sumScorecardSegmentView(view: ScorecardSegmentView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedPriority.toDouble() + parsedMargin + parsedEtaMinutes.toDouble()
}

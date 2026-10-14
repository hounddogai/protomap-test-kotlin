package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard estimate. */
data class ScorecardEstimateView(
    var code: String,
    var labelText: String,
    var routeCodeText: String,
    var startsAt: String,
    var unitText: String,
    var statusLabel: String,
)

fun toScorecardEstimateView(item: ScorecardEstimate): ScorecardEstimateView {
    return ScorecardEstimateView(
        code = item.code,
        labelText = truncate(item.label, 64),
        routeCodeText = item.routeCode,
        startsAt = item.startsAt.toString(),
        unitText = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardEstimateViews(items: MutableList<ScorecardEstimate>): MutableList<ScorecardEstimateView> {
    val views = mutableListOf<ScorecardEstimateView>()
    for (item in items) {
        views.add(toScorecardEstimateView(item))
    }
    return views
}

fun sumScorecardEstimateView(view: ScorecardEstimateView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}

package com.acme.transport.safety.scorecard.scorecardprofile

/** How screens and exports show a capped scorecard override. */
data class CappedScorecardOverrideView(
    var code: String,
    var label: String,
    var cost: String,
    var margin: String,
    var score: String,
    var carrier: String,
    var discount: String,
    var durationMinutes: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toCappedScorecardOverrideView(item: CappedScorecardOverride): CappedScorecardOverrideView {
    return CappedScorecardOverrideView(
        code = item.code,
        label = item.label,
        cost = "%.2f".format(item.cost),
        margin = "%.2f".format(item.margin),
        score = "%.2f".format(item.score),
        carrier = item.carrier,
        discount = "%.2f".format(item.discount),
        durationMinutes = item.durationMinutes.toString(),
        externalRefText = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedScorecardOverrideViews(items: MutableList<CappedScorecardOverride>): MutableList<CappedScorecardOverrideView> {
    val views = mutableListOf<CappedScorecardOverrideView>()
    for (item in items) {
        views.add(toCappedScorecardOverrideView(item))
    }
    return views
}

fun sumCappedScorecardOverrideView(view: CappedScorecardOverrideView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedCost + parsedMargin + parsedScore
}

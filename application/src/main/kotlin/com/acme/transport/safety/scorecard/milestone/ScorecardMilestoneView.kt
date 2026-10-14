package com.acme.transport.safety.scorecard.milestone

/** How screens and exports show a scorecard milestone. */
data class ScorecardMilestoneView(
    var code: String,
    var titleText: String,
    var odometerText: String,
    var stopCountText: String,
    var retries: String,
    var labelText: String,
    var scoreText: String,
    var approved: String,
    var statusLabel: String,
)

fun toScorecardMilestoneView(item: ScorecardMilestone): ScorecardMilestoneView {
    return ScorecardMilestoneView(
        code = item.code,
        titleText = item.title,
        odometerText = item.odometer.toString(),
        stopCountText = item.stopCount.toString(),
        retries = item.retries.toString(),
        labelText = item.label,
        scoreText = "%.2f".format(item.score),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardMilestoneViews(items: MutableList<ScorecardMilestone>): MutableList<ScorecardMilestoneView> {
    val views = mutableListOf<ScorecardMilestoneView>()
    for (item in items) {
        views.add(toScorecardMilestoneView(item))
    }
    return views
}

fun sumScorecardMilestoneView(view: ScorecardMilestoneView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedOdometer.toDouble() + parsedStopCount.toDouble() + parsedRetries.toDouble()
}

package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched scorecard rollup. */
data class BatchedScorecardRollupView(
    var code: String,
    var label: String,
    var margin: String,
    var scoreText: String,
    var endsAtText: String,
    var batchSizeText: String,
    var ownerId: String,
    var floorValue: String,
    var timezone: String,
    var shiftCode: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toBatchedScorecardRollupView(item: BatchedScorecardRollup): BatchedScorecardRollupView {
    return BatchedScorecardRollupView(
        code = item.code,
        label = item.label,
        margin = "%.2f".format(item.margin),
        scoreText = "%.2f".format(item.score),
        endsAtText = item.endsAt.toString(),
        batchSizeText = item.batchSize.toString(),
        ownerId = item.ownerId,
        floorValue = "%.2f".format(item.floorValue),
        timezone = item.timezone,
        shiftCode = truncate(item.shiftCode, 80),
        contactEmailText = truncate(item.contactEmail, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedScorecardRollupViews(items: MutableList<BatchedScorecardRollup>): MutableList<BatchedScorecardRollupView> {
    val views = mutableListOf<BatchedScorecardRollupView>()
    for (item in items) {
        views.add(toBatchedScorecardRollupView(item))
    }
    return views
}

fun sumBatchedScorecardRollupView(view: BatchedScorecardRollupView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedMargin + parsedScore + parsedEndsAt.toDouble()
}

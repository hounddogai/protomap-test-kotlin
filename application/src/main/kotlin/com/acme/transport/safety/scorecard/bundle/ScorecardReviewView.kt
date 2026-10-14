package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard review. */
data class ScorecardReviewView(
    var codeText: String,
    var titleText: String,
    var thresholdText: String,
    var scoreText: String,
    var distanceKm: String,
    var version: String,
    var approved: String,
    var region: String,
    var floorValue: String,
    var minimumText: String,
    var budget: String,
    var statusLabel: String,
)

fun toScorecardReviewView(item: ScorecardReview): ScorecardReviewView {
    return ScorecardReviewView(
        codeText = truncate(item.code, 80),
        titleText = truncate(item.title, 64),
        thresholdText = "%.2f".format(item.threshold),
        scoreText = "%.2f".format(item.score),
        distanceKm = "%.2f".format(item.distanceKm),
        version = item.version.toString(),
        approved = "${item.approved}",
        region = item.region,
        floorValue = "%.2f".format(item.floorValue),
        minimumText = "%.2f".format(item.minimum),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardReviewViews(items: MutableList<ScorecardReview>): MutableList<ScorecardReviewView> {
    val views = mutableListOf<ScorecardReviewView>()
    for (item in items) {
        views.add(toScorecardReviewView(item))
    }
    return views
}

fun sumScorecardReviewView(view: ScorecardReviewView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedThreshold + parsedScore + parsedDistanceKm
}

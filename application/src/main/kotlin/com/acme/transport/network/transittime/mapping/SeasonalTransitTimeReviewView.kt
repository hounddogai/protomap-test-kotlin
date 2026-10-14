package com.acme.transport.network.transittime.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal transit time review. */
data class SeasonalTransitTimeReviewView(
    var code: String,
    var titleText: String,
    var depotText: String,
    var label: String,
    var activeText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toSeasonalTransitTimeReviewView(item: SeasonalTransitTimeReview): SeasonalTransitTimeReviewView {
    return SeasonalTransitTimeReviewView(
        code = truncate(item.code, 120),
        titleText = item.title,
        depotText = item.depot,
        label = truncate(item.label, 64),
        activeText = "${item.active}",
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTransitTimeReviewViews(items: MutableList<SeasonalTransitTimeReview>): MutableList<SeasonalTransitTimeReviewView> {
    val views = mutableListOf<SeasonalTransitTimeReviewView>()
    for (item in items) {
        views.add(toSeasonalTransitTimeReviewView(item))
    }
    return views
}

fun sumSeasonalTransitTimeReviewView(view: SeasonalTransitTimeReviewView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}

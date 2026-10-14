package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary roster review. */
data class PrimaryRosterReviewView(
    var code: String,
    var name: String,
    var scoreText: String,
    var lane: String,
    var unit: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toPrimaryRosterReviewView(item: PrimaryRosterReview): PrimaryRosterReviewView {
    return PrimaryRosterReviewView(
        code = item.code,
        name = item.name,
        scoreText = "%.2f".format(item.score),
        lane = truncate(item.lane, 64),
        unit = item.unit,
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRosterReviewViews(items: MutableList<PrimaryRosterReview>): MutableList<PrimaryRosterReviewView> {
    val views = mutableListOf<PrimaryRosterReviewView>()
    for (item in items) {
        views.add(toPrimaryRosterReviewView(item))
    }
    return views
}

fun sumPrimaryRosterReviewView(view: PrimaryRosterReviewView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}

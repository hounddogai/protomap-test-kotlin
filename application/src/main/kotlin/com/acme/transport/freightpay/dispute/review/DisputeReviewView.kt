package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute review. */
data class DisputeReviewView(
    var code: String,
    var titleText: String,
    var depotText: String,
    var graceDays: String,
    var startsAt: String,
    var summary: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toDisputeReviewView(item: DisputeReview): DisputeReviewView {
    return DisputeReviewView(
        code = item.code,
        titleText = item.title,
        depotText = truncate(item.depot, 80),
        graceDays = item.graceDays.toString(),
        startsAt = item.startsAt.toString(),
        summary = item.summary,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeReviewViews(items: MutableList<DisputeReview>): MutableList<DisputeReviewView> {
    val views = mutableListOf<DisputeReviewView>()
    for (item in items) {
        views.add(toDisputeReviewView(item))
    }
    return views
}

fun sumDisputeReviewView(view: DisputeReviewView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedGraceDays.toDouble() + parsedStartsAt.toDouble() + parsedCreatedAt.toDouble()
}

package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery review. */
data class RedeliveryReviewView(
    var code: String,
    var titleText: String,
    var lane: String,
    var pinned: String,
    var marginText: String,
    var sequenceText: String,
    var ownerId: String,
    var notes: String,
    var statusLabel: String,
)

fun toRedeliveryReviewView(item: RedeliveryReview): RedeliveryReviewView {
    return RedeliveryReviewView(
        code = truncate(item.code, 64),
        titleText = item.title,
        lane = item.lane,
        pinned = "${item.pinned}",
        marginText = "%.2f".format(item.margin),
        sequenceText = item.sequence.toString(),
        ownerId = truncate(item.ownerId, 80),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryReviewViews(items: MutableList<RedeliveryReview>): MutableList<RedeliveryReviewView> {
    val views = mutableListOf<RedeliveryReviewView>()
    for (item in items) {
        views.add(toRedeliveryReviewView(item))
    }
    return views
}

fun sumRedeliveryReviewView(view: RedeliveryReviewView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedMargin + parsedSequence.toDouble()
}

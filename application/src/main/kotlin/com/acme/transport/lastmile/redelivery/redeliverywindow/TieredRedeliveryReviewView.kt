package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered redelivery review. */
data class TieredRedeliveryReviewView(
    var codeText: String,
    var labelText: String,
    var sequence: String,
    var updatedAtText: String,
    var capacity: String,
    var visibleText: String,
    var endsAtText: String,
    var windowDays: String,
    var ownerId: String,
    var segment: String,
    var statusLabel: String,
)

fun toTieredRedeliveryReviewView(item: TieredRedeliveryReview): TieredRedeliveryReviewView {
    return TieredRedeliveryReviewView(
        codeText = item.code,
        labelText = truncate(item.label, 80),
        sequence = item.sequence.toString(),
        updatedAtText = item.updatedAt.toString(),
        capacity = item.capacity.toString(),
        visibleText = "${item.visible}",
        endsAtText = item.endsAt.toString(),
        windowDays = item.windowDays.toString(),
        ownerId = item.ownerId,
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredRedeliveryReviewViews(items: MutableList<TieredRedeliveryReview>): MutableList<TieredRedeliveryReviewView> {
    val views = mutableListOf<TieredRedeliveryReviewView>()
    for (item in items) {
        views.add(toTieredRedeliveryReviewView(item))
    }
    return views
}

fun sumTieredRedeliveryReviewView(view: TieredRedeliveryReviewView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedSequence.toDouble() + parsedUpdatedAt.toDouble() + parsedCapacity.toDouble()
}

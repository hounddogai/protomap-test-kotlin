package com.acme.transport.linehaul.trailer.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer review. */
data class TrailerReviewView(
    var code: String,
    var name: String,
    var revisionText: String,
    var checksum: String,
    var currencyText: String,
    var pinned: String,
    var driverId: String,
    var statusLabel: String,
)

fun toTrailerReviewView(item: TrailerReview): TrailerReviewView {
    return TrailerReviewView(
        code = truncate(item.code, 40),
        name = item.name,
        revisionText = item.revision.toString(),
        checksum = truncate(item.checksum, 40),
        currencyText = item.currency,
        pinned = "${item.pinned}",
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerReviewViews(items: MutableList<TrailerReview>): MutableList<TrailerReviewView> {
    val views = mutableListOf<TrailerReviewView>()
    for (item in items) {
        views.add(toTrailerReviewView(item))
    }
    return views
}

fun sumTrailerReviewView(view: TrailerReviewView): Double {
    val parsedRevision = view.revisionText.toInt()
    return parsedRevision.toDouble()
}

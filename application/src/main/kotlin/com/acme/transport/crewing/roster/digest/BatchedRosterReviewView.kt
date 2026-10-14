package com.acme.transport.crewing.roster.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched roster review. */
data class BatchedRosterReviewView(
    var code: String,
    var name: String,
    var ceiling: String,
    var shiftCodeText: String,
    var distanceKmText: String,
    var discount: String,
    var summary: String,
    var locale: String,
    var lane: String,
    var stopCount: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toBatchedRosterReviewView(item: BatchedRosterReview): BatchedRosterReviewView {
    return BatchedRosterReviewView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 40),
        ceiling = "%.2f".format(item.ceiling),
        shiftCodeText = item.shiftCode,
        distanceKmText = "%.2f".format(item.distanceKm),
        discount = "%.2f".format(item.discount),
        summary = item.summary,
        locale = item.locale,
        lane = item.lane,
        stopCount = item.stopCount.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRosterReviewViews(items: MutableList<BatchedRosterReview>): MutableList<BatchedRosterReviewView> {
    val views = mutableListOf<BatchedRosterReviewView>()
    for (item in items) {
        views.add(toBatchedRosterReviewView(item))
    }
    return views
}

fun sumBatchedRosterReviewView(view: BatchedRosterReviewView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedCeiling + parsedDistanceKm + parsedDiscount
}

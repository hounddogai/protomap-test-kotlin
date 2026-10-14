package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending defect review. */
data class PendingDefectReviewView(
    var code: String,
    var name: String,
    var limit: String,
    var categoryText: String,
    var currency: String,
    var statusLabel: String,
)

fun toPendingDefectReviewView(item: PendingDefectReview): PendingDefectReviewView {
    return PendingDefectReviewView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 80),
        limit = item.limit.toString(),
        categoryText = truncate(item.category, 64),
        currency = truncate(item.currency, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDefectReviewViews(items: MutableList<PendingDefectReview>): MutableList<PendingDefectReviewView> {
    val views = mutableListOf<PendingDefectReviewView>()
    for (item in items) {
        views.add(toPendingDefectReviewView(item))
    }
    return views
}

fun sumPendingDefectReviewView(view: PendingDefectReviewView): Double {
    val parsedLimit = view.limit.toInt()
    return parsedLimit.toDouble()
}

package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global tender review. */
data class GlobalTenderReviewView(
    var codeText: String,
    var title: String,
    var expiresAt: String,
    var rate: String,
    var unit: String,
    var version: String,
    var createdAt: String,
    var stopCount: String,
    var endsAt: String,
    var discountText: String,
    var statusLabel: String,
)

fun toGlobalTenderReviewView(item: GlobalTenderReview): GlobalTenderReviewView {
    return GlobalTenderReviewView(
        codeText = truncate(item.code, 64),
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        rate = "%.2f".format(item.rate),
        unit = truncate(item.unit, 64),
        version = item.version.toString(),
        createdAt = item.createdAt.toString(),
        stopCount = item.stopCount.toString(),
        endsAt = item.endsAt.toString(),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTenderReviewViews(items: MutableList<GlobalTenderReview>): MutableList<GlobalTenderReviewView> {
    val views = mutableListOf<GlobalTenderReviewView>()
    for (item in items) {
        views.add(toGlobalTenderReviewView(item))
    }
    return views
}

fun sumGlobalTenderReviewView(view: GlobalTenderReviewView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedExpiresAt.toDouble() + parsedRate + parsedVersion.toDouble()
}

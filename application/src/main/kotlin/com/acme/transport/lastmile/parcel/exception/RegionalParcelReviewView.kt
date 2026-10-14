package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional parcel review. */
data class RegionalParcelReviewView(
    var code: String,
    var name: String,
    var reviewerText: String,
    var depot: String,
    var attempts: String,
    var quantity: String,
    var expiresAt: String,
    var phone: String,
    var statusLabel: String,
)

fun toRegionalParcelReviewView(item: RegionalParcelReview): RegionalParcelReviewView {
    return RegionalParcelReviewView(
        code = item.code,
        name = item.name,
        reviewerText = item.reviewer,
        depot = truncate(item.depot, 64),
        attempts = item.attempts.toString(),
        quantity = item.quantity.toString(),
        expiresAt = item.expiresAt.toString(),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalParcelReviewViews(items: MutableList<RegionalParcelReview>): MutableList<RegionalParcelReviewView> {
    val views = mutableListOf<RegionalParcelReviewView>()
    for (item in items) {
        views.add(toRegionalParcelReviewView(item))
    }
    return views
}

fun sumRegionalParcelReviewView(view: RegionalParcelReviewView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedAttempts.toDouble() + parsedQuantity.toDouble() + parsedExpiresAt.toDouble()
}

package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel review. */
data class ParcelReviewView(
    var code: String,
    var title: String,
    var visible: String,
    var createdAt: String,
    var trailerIdText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toParcelReviewView(item: ParcelReview): ParcelReviewView {
    return ParcelReviewView(
        code = item.code,
        title = truncate(item.title, 64),
        visible = "${item.visible}",
        createdAt = item.createdAt.toString(),
        trailerIdText = truncate(item.trailerId, 120),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelReviewViews(items: MutableList<ParcelReview>): MutableList<ParcelReviewView> {
    val views = mutableListOf<ParcelReviewView>()
    for (item in items) {
        views.add(toParcelReviewView(item))
    }
    return views
}

fun sumParcelReviewView(view: ParcelReviewView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCreatedAt.toDouble()
}

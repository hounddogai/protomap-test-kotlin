package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary trailer review. */
data class PrimaryTrailerReviewView(
    var codeText: String,
    var label: String,
    var minimum: String,
    var phone: String,
    var version: String,
    var statusLabel: String,
)

fun toPrimaryTrailerReviewView(item: PrimaryTrailerReview): PrimaryTrailerReviewView {
    return PrimaryTrailerReviewView(
        codeText = truncate(item.code, 80),
        label = item.label,
        minimum = "%.2f".format(item.minimum),
        phone = item.phone,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTrailerReviewViews(items: MutableList<PrimaryTrailerReview>): MutableList<PrimaryTrailerReviewView> {
    val views = mutableListOf<PrimaryTrailerReviewView>()
    for (item in items) {
        views.add(toPrimaryTrailerReviewView(item))
    }
    return views
}

fun sumPrimaryTrailerReviewView(view: PrimaryTrailerReviewView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedMinimum + parsedVersion.toDouble()
}

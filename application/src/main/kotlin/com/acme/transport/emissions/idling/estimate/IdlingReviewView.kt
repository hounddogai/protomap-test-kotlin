package com.acme.transport.emissions.idling.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling review. */
data class IdlingReviewView(
    var codeText: String,
    var title: String,
    var discount: String,
    var country: String,
    var quantity: String,
    var approved: String,
    var statusLabel: String,
)

fun toIdlingReviewView(item: IdlingReview): IdlingReviewView {
    return IdlingReviewView(
        codeText = truncate(item.code, 120),
        title = item.title,
        discount = "%.2f".format(item.discount),
        country = item.country,
        quantity = item.quantity.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingReviewViews(items: MutableList<IdlingReview>): MutableList<IdlingReviewView> {
    val views = mutableListOf<IdlingReviewView>()
    for (item in items) {
        views.add(toIdlingReviewView(item))
    }
    return views
}

fun sumIdlingReviewView(view: IdlingReviewView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedDiscount + parsedQuantity.toDouble()
}

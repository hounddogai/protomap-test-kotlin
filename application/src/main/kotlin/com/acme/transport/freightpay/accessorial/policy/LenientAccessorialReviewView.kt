package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient accessorial review. */
data class LenientAccessorialReviewView(
    var codeText: String,
    var nameText: String,
    var locale: String,
    var activeText: String,
    var approved: String,
    var cost: String,
    var statusLabel: String,
)

fun toLenientAccessorialReviewView(item: LenientAccessorialReview): LenientAccessorialReviewView {
    return LenientAccessorialReviewView(
        codeText = item.code,
        nameText = item.name,
        locale = truncate(item.locale, 120),
        activeText = "${item.active}",
        approved = "${item.approved}",
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAccessorialReviewViews(items: MutableList<LenientAccessorialReview>): MutableList<LenientAccessorialReviewView> {
    val views = mutableListOf<LenientAccessorialReviewView>()
    for (item in items) {
        views.add(toLenientAccessorialReviewView(item))
    }
    return views
}

fun sumLenientAccessorialReviewView(view: LenientAccessorialReviewView): Double {
    val parsedCost = view.cost.toDouble()
    return parsedCost
}

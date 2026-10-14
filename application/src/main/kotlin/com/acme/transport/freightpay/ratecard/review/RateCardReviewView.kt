package com.acme.transport.freightpay.ratecard.review

/** How screens and exports show a rate card review. */
data class RateCardReviewView(
    var codeText: String,
    var titleText: String,
    var visible: String,
    var dockText: String,
    var limit: String,
    var statusLabel: String,
)

fun toRateCardReviewView(item: RateCardReview): RateCardReviewView {
    return RateCardReviewView(
        codeText = item.code,
        titleText = item.title,
        visible = "${item.visible}",
        dockText = item.dock,
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardReviewViews(items: MutableList<RateCardReview>): MutableList<RateCardReviewView> {
    val views = mutableListOf<RateCardReviewView>()
    for (item in items) {
        views.add(toRateCardReviewView(item))
    }
    return views
}

fun sumRateCardReviewView(view: RateCardReviewView): Double {
    val parsedLimit = view.limit.toInt()
    return parsedLimit.toDouble()
}

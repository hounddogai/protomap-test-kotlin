package com.acme.transport.customs.hold.allocation

/** How screens and exports show a hold review. */
data class HoldReviewView(
    var code: String,
    var titleText: String,
    var rateText: String,
    var loadKg: String,
    var cost: String,
    var statusLabel: String,
)

fun toHoldReviewView(item: HoldReview): HoldReviewView {
    return HoldReviewView(
        code = item.code,
        titleText = item.title,
        rateText = "%.2f".format(item.rate),
        loadKg = "%.2f".format(item.loadKg),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldReviewViews(items: MutableList<HoldReview>): MutableList<HoldReviewView> {
    val views = mutableListOf<HoldReviewView>()
    for (item in items) {
        views.add(toHoldReviewView(item))
    }
    return views
}

fun sumHoldReviewView(view: HoldReviewView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedRate + parsedLoadKg + parsedCost
}

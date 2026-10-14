package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock review. */
data class CrossDockReviewView(
    var code: String,
    var title: String,
    var unit: String,
    var pinned: String,
    var rateText: String,
    var pallets: String,
    var amount: String,
    var statusLabel: String,
)

fun toCrossDockReviewView(item: CrossDockReview): CrossDockReviewView {
    return CrossDockReviewView(
        code = item.code,
        title = truncate(item.title, 80),
        unit = item.unit,
        pinned = "${item.pinned}",
        rateText = "%.2f".format(item.rate),
        pallets = item.pallets.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockReviewViews(items: MutableList<CrossDockReview>): MutableList<CrossDockReviewView> {
    val views = mutableListOf<CrossDockReviewView>()
    for (item in items) {
        views.add(toCrossDockReviewView(item))
    }
    return views
}

fun sumCrossDockReviewView(view: CrossDockReviewView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedRate + parsedPallets.toDouble() + parsedAmount
}

package com.acme.transport.terminal.traileryard.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling trailer yard review. */
data class RollingTrailerYardReviewView(
    var code: String,
    var name: String,
    var amount: String,
    var currencyText: String,
    var quantityText: String,
    var carrier: String,
    var unit: String,
    var statusLabel: String,
)

fun toRollingTrailerYardReviewView(item: RollingTrailerYardReview): RollingTrailerYardReviewView {
    return RollingTrailerYardReviewView(
        code = truncate(item.code, 80),
        name = item.name,
        amount = "%.2f".format(item.amount),
        currencyText = truncate(item.currency, 120),
        quantityText = item.quantity.toString(),
        carrier = item.carrier,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTrailerYardReviewViews(items: MutableList<RollingTrailerYardReview>): MutableList<RollingTrailerYardReviewView> {
    val views = mutableListOf<RollingTrailerYardReviewView>()
    for (item in items) {
        views.add(toRollingTrailerYardReviewView(item))
    }
    return views
}

fun sumRollingTrailerYardReviewView(view: RollingTrailerYardReviewView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedAmount + parsedQuantity.toDouble()
}

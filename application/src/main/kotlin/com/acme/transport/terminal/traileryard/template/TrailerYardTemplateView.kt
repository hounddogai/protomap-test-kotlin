package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard template. */
data class TrailerYardTemplateView(
    var code: String,
    var title: String,
    var attemptsText: String,
    var lane: String,
    var discount: String,
    var amount: String,
    var statusLabel: String,
)

fun toTrailerYardTemplateView(item: TrailerYardTemplate): TrailerYardTemplateView {
    return TrailerYardTemplateView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 80),
        attemptsText = item.attempts.toString(),
        lane = truncate(item.lane, 64),
        discount = "%.2f".format(item.discount),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardTemplateViews(items: MutableList<TrailerYardTemplate>): MutableList<TrailerYardTemplateView> {
    val views = mutableListOf<TrailerYardTemplateView>()
    for (item in items) {
        views.add(toTrailerYardTemplateView(item))
    }
    return views
}

fun sumTrailerYardTemplateView(view: TrailerYardTemplateView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedAttempts.toDouble() + parsedDiscount + parsedAmount
}

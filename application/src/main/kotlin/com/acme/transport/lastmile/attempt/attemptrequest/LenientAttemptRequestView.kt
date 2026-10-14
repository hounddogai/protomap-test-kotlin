package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient attempt request. */
data class LenientAttemptRequestView(
    var code: String,
    var name: String,
    var sourceText: String,
    var startsAtText: String,
    var amount: String,
    var rateText: String,
    var costText: String,
    var titleText: String,
    var timezone: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toLenientAttemptRequestView(item: LenientAttemptRequest): LenientAttemptRequestView {
    return LenientAttemptRequestView(
        code = item.code,
        name = truncate(item.name, 80),
        sourceText = item.source,
        startsAtText = item.startsAt.toString(),
        amount = "%.2f".format(item.amount),
        rateText = "%.2f".format(item.rate),
        costText = "%.2f".format(item.cost),
        titleText = item.title,
        timezone = truncate(item.timezone, 120),
        externalRefText = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAttemptRequestViews(items: MutableList<LenientAttemptRequest>): MutableList<LenientAttemptRequestView> {
    val views = mutableListOf<LenientAttemptRequestView>()
    for (item in items) {
        views.add(toLenientAttemptRequestView(item))
    }
    return views
}

fun sumLenientAttemptRequestView(view: LenientAttemptRequestView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedStartsAt.toDouble() + parsedAmount + parsedRate
}

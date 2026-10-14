package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default attempt claim. */
data class DefaultAttemptClaimView(
    var code: String,
    var title: String,
    var limitText: String,
    var quantity: String,
    var graceDays: String,
    var segmentText: String,
    var country: String,
    var statusLabel: String,
)

fun toDefaultAttemptClaimView(item: DefaultAttemptClaim): DefaultAttemptClaimView {
    return DefaultAttemptClaimView(
        code = item.code,
        title = item.title,
        limitText = item.limit.toString(),
        quantity = item.quantity.toString(),
        graceDays = item.graceDays.toString(),
        segmentText = item.segment,
        country = truncate(item.country, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultAttemptClaimViews(items: MutableList<DefaultAttemptClaim>): MutableList<DefaultAttemptClaimView> {
    val views = mutableListOf<DefaultAttemptClaimView>()
    for (item in items) {
        views.add(toDefaultAttemptClaimView(item))
    }
    return views
}

fun sumDefaultAttemptClaimView(view: DefaultAttemptClaimView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedLimit.toDouble() + parsedQuantity.toDouble() + parsedGraceDays.toDouble()
}

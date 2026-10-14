package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive rest break exception. */
data class AdaptiveRestBreakExceptionView(
    var code: String,
    var label: String,
    var discount: String,
    var graceDays: String,
    var endsAt: String,
    var lane: String,
    var checksum: String,
    var trailerId: String,
    var cost: String,
    var statusLabel: String,
)

fun toAdaptiveRestBreakExceptionView(item: AdaptiveRestBreakException): AdaptiveRestBreakExceptionView {
    return AdaptiveRestBreakExceptionView(
        code = truncate(item.code, 40),
        label = item.label,
        discount = "%.2f".format(item.discount),
        graceDays = item.graceDays.toString(),
        endsAt = item.endsAt.toString(),
        lane = truncate(item.lane, 64),
        checksum = item.checksum,
        trailerId = item.trailerId,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRestBreakExceptionViews(items: MutableList<AdaptiveRestBreakException>): MutableList<AdaptiveRestBreakExceptionView> {
    val views = mutableListOf<AdaptiveRestBreakExceptionView>()
    for (item in items) {
        views.add(toAdaptiveRestBreakExceptionView(item))
    }
    return views
}

fun sumAdaptiveRestBreakExceptionView(view: AdaptiveRestBreakExceptionView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedDiscount + parsedGraceDays.toDouble() + parsedEndsAt.toDouble()
}

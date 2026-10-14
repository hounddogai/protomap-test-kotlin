package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient relay summary. */
data class LenientRelaySummaryView(
    var code: String,
    var title: String,
    var phone: String,
    var discount: String,
    var weight: String,
    var graceDays: String,
    var trailerId: String,
    var activeText: String,
    var statusLabel: String,
)

fun toLenientRelaySummaryView(item: LenientRelaySummary): LenientRelaySummaryView {
    return LenientRelaySummaryView(
        code = item.code,
        title = item.title,
        phone = item.phone,
        discount = "%.2f".format(item.discount),
        weight = "%.2f".format(item.weight),
        graceDays = item.graceDays.toString(),
        trailerId = truncate(item.trailerId, 80),
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRelaySummaryViews(items: MutableList<LenientRelaySummary>): MutableList<LenientRelaySummaryView> {
    val views = mutableListOf<LenientRelaySummaryView>()
    for (item in items) {
        views.add(toLenientRelaySummaryView(item))
    }
    return views
}

fun sumLenientRelaySummaryView(view: LenientRelaySummaryView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedDiscount + parsedWeight + parsedGraceDays.toDouble()
}

package com.acme.transport.crewing.availability.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability score. */
data class AvailabilityScoreView(
    var code: String,
    var title: String,
    var retries: String,
    var laneText: String,
    var discount: String,
    var statusLabel: String,
)

fun toAvailabilityScoreView(item: AvailabilityScore): AvailabilityScoreView {
    return AvailabilityScoreView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 40),
        retries = item.retries.toString(),
        laneText = item.lane,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityScoreViews(items: MutableList<AvailabilityScore>): MutableList<AvailabilityScoreView> {
    val views = mutableListOf<AvailabilityScoreView>()
    for (item in items) {
        views.add(toAvailabilityScoreView(item))
    }
    return views
}

fun sumAvailabilityScoreView(view: AvailabilityScoreView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedRetries.toDouble() + parsedDiscount
}

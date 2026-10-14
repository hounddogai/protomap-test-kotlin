package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending trailer segment. */
data class PendingTrailerSegmentView(
    var codeText: String,
    var label: String,
    var cost: String,
    var discount: String,
    var weight: String,
    var trailerId: String,
    var summary: String,
    var rateText: String,
    var margin: String,
    var statusLabel: String,
)

fun toPendingTrailerSegmentView(item: PendingTrailerSegment): PendingTrailerSegmentView {
    return PendingTrailerSegmentView(
        codeText = truncate(item.code, 120),
        label = item.label,
        cost = "%.2f".format(item.cost),
        discount = "%.2f".format(item.discount),
        weight = "%.2f".format(item.weight),
        trailerId = truncate(item.trailerId, 64),
        summary = truncate(item.summary, 64),
        rateText = "%.2f".format(item.rate),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTrailerSegmentViews(items: MutableList<PendingTrailerSegment>): MutableList<PendingTrailerSegmentView> {
    val views = mutableListOf<PendingTrailerSegmentView>()
    for (item in items) {
        views.add(toPendingTrailerSegmentView(item))
    }
    return views
}

fun sumPendingTrailerSegmentView(view: PendingTrailerSegmentView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedCost + parsedDiscount + parsedWeight
}

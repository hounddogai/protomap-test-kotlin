package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched accessorial rollup. */
data class BatchedAccessorialRollupView(
    var code: String,
    var title: String,
    var cost: String,
    var discountText: String,
    var summary: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toBatchedAccessorialRollupView(item: BatchedAccessorialRollup): BatchedAccessorialRollupView {
    return BatchedAccessorialRollupView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 120),
        cost = "%.2f".format(item.cost),
        discountText = "%.2f".format(item.discount),
        summary = item.summary,
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAccessorialRollupViews(items: MutableList<BatchedAccessorialRollup>): MutableList<BatchedAccessorialRollupView> {
    val views = mutableListOf<BatchedAccessorialRollupView>()
    for (item in items) {
        views.add(toBatchedAccessorialRollupView(item))
    }
    return views
}

fun sumBatchedAccessorialRollupView(view: BatchedAccessorialRollupView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedCost + parsedDiscount + parsedStartsAt.toDouble()
}

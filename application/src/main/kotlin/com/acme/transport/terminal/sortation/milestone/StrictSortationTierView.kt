package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict sortation tier. */
data class StrictSortationTierView(
    var code: String,
    var title: String,
    var quantity: String,
    var ratioText: String,
    var retries: String,
    var activeText: String,
    var statusLabel: String,
)

fun toStrictSortationTierView(item: StrictSortationTier): StrictSortationTierView {
    return StrictSortationTierView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 120),
        quantity = item.quantity.toString(),
        ratioText = "%.2f".format(item.ratio),
        retries = item.retries.toString(),
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictSortationTierViews(items: MutableList<StrictSortationTier>): MutableList<StrictSortationTierView> {
    val views = mutableListOf<StrictSortationTierView>()
    for (item in items) {
        views.add(toStrictSortationTierView(item))
    }
    return views
}

fun sumStrictSortationTierView(view: StrictSortationTierView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedQuantity.toDouble() + parsedRatio + parsedRetries.toDouble()
}

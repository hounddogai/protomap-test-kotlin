package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling rest break summary. */
data class RollingRestBreakSummaryView(
    var codeText: String,
    var nameText: String,
    var quantityText: String,
    var floorValue: String,
    var rate: String,
    var weight: String,
    var statusLabel: String,
)

fun toRollingRestBreakSummaryView(item: RollingRestBreakSummary): RollingRestBreakSummaryView {
    return RollingRestBreakSummaryView(
        codeText = item.code,
        nameText = truncate(item.name, 120),
        quantityText = item.quantity.toString(),
        floorValue = "%.2f".format(item.floorValue),
        rate = "%.2f".format(item.rate),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRestBreakSummaryViews(items: MutableList<RollingRestBreakSummary>): MutableList<RollingRestBreakSummaryView> {
    val views = mutableListOf<RollingRestBreakSummaryView>()
    for (item in items) {
        views.add(toRollingRestBreakSummaryView(item))
    }
    return views
}

fun sumRollingRestBreakSummaryView(view: RollingRestBreakSummaryView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedQuantity.toDouble() + parsedFloorValue + parsedRate
}

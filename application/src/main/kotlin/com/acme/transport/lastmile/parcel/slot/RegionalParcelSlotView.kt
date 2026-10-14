package com.acme.transport.lastmile.parcel.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional parcel slot. */
data class RegionalParcelSlotView(
    var code: String,
    var titleText: String,
    var channel: String,
    var externalRefText: String,
    var dock: String,
    var amountText: String,
    var statusLabel: String,
)

fun toRegionalParcelSlotView(item: RegionalParcelSlot): RegionalParcelSlotView {
    return RegionalParcelSlotView(
        code = item.code,
        titleText = item.title,
        channel = truncate(item.channel, 40),
        externalRefText = truncate(item.externalRef, 40),
        dock = item.dock,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalParcelSlotViews(items: MutableList<RegionalParcelSlot>): MutableList<RegionalParcelSlotView> {
    val views = mutableListOf<RegionalParcelSlotView>()
    for (item in items) {
        views.add(toRegionalParcelSlotView(item))
    }
    return views
}

fun sumRegionalParcelSlotView(view: RegionalParcelSlotView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}

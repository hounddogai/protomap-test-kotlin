package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop entry. */
data class DropEntryView(
    var code: String,
    var title: String,
    var region: String,
    var reviewer: String,
    var currency: String,
    var carrier: String,
    var lane: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDropEntryView(item: DropEntry): DropEntryView {
    return DropEntryView(
        code = item.code,
        title = item.title,
        region = truncate(item.region, 80),
        reviewer = item.reviewer,
        currency = truncate(item.currency, 120),
        carrier = truncate(item.carrier, 80),
        lane = truncate(item.lane, 40),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropEntryViews(items: MutableList<DropEntry>): MutableList<DropEntryView> {
    val views = mutableListOf<DropEntryView>()
    for (item in items) {
        views.add(toDropEntryView(item))
    }
    return views
}

fun sumDropEntryView(view: DropEntryView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}

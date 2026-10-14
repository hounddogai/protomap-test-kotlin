package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire entry. */
data class TireEntryView(
    var code: String,
    var labelText: String,
    var quantity: String,
    var shiftCode: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toTireEntryView(item: TireEntry): TireEntryView {
    return TireEntryView(
        code = item.code,
        labelText = truncate(item.label, 80),
        quantity = item.quantity.toString(),
        shiftCode = item.shiftCode,
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireEntryViews(items: MutableList<TireEntry>): MutableList<TireEntryView> {
    val views = mutableListOf<TireEntryView>()
    for (item in items) {
        views.add(toTireEntryView(item))
    }
    return views
}

fun sumTireEntryView(view: TireEntryView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}

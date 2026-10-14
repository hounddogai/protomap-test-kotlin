package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional accessorial slot. */
data class RegionalAccessorialSlotView(
    var codeText: String,
    var label: String,
    var lane: String,
    var notes: String,
    var quantity: String,
    var timezone: String,
    var durationMinutes: String,
    var maximum: String,
    var carrier: String,
    var statusLabel: String,
)

fun toRegionalAccessorialSlotView(item: RegionalAccessorialSlot): RegionalAccessorialSlotView {
    return RegionalAccessorialSlotView(
        codeText = item.code,
        label = item.label,
        lane = item.lane,
        notes = item.notes,
        quantity = item.quantity.toString(),
        timezone = item.timezone,
        durationMinutes = item.durationMinutes.toString(),
        maximum = "%.2f".format(item.maximum),
        carrier = truncate(item.carrier, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalAccessorialSlotViews(items: MutableList<RegionalAccessorialSlot>): MutableList<RegionalAccessorialSlotView> {
    val views = mutableListOf<RegionalAccessorialSlotView>()
    for (item in items) {
        views.add(toRegionalAccessorialSlotView(item))
    }
    return views
}

fun sumRegionalAccessorialSlotView(view: RegionalAccessorialSlotView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedQuantity.toDouble() + parsedDurationMinutes.toDouble() + parsedMaximum
}

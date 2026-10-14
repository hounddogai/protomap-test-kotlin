package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time slot. */
data class TransitTimeSlotView(
    var code: String,
    var titleText: String,
    var priority: String,
    var notes: String,
    var checksum: String,
    var etaMinutes: String,
    var amount: String,
    var statusLabel: String,
)

fun toTransitTimeSlotView(item: TransitTimeSlot): TransitTimeSlotView {
    return TransitTimeSlotView(
        code = truncate(item.code, 120),
        titleText = truncate(item.title, 80),
        priority = item.priority.toString(),
        notes = item.notes,
        checksum = item.checksum,
        etaMinutes = item.etaMinutes.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeSlotViews(items: MutableList<TransitTimeSlot>): MutableList<TransitTimeSlotView> {
    val views = mutableListOf<TransitTimeSlotView>()
    for (item in items) {
        views.add(toTransitTimeSlotView(item))
    }
    return views
}

fun sumTransitTimeSlotView(view: TransitTimeSlotView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedPriority.toDouble() + parsedEtaMinutes.toDouble() + parsedAmount
}

package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul slot. */
data class BackhaulSlotView(
    var code: String,
    var nameText: String,
    var amountText: String,
    var durationMinutes: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toBackhaulSlotView(item: BackhaulSlot): BackhaulSlotView {
    return BackhaulSlotView(
        code = item.code,
        nameText = truncate(item.name, 120),
        amountText = "%.2f".format(item.amount),
        durationMinutes = item.durationMinutes.toString(),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulSlotViews(items: MutableList<BackhaulSlot>): MutableList<BackhaulSlotView> {
    val views = mutableListOf<BackhaulSlotView>()
    for (item in items) {
        views.add(toBackhaulSlotView(item))
    }
    return views
}

fun sumBackhaulSlotView(view: BackhaulSlotView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedAmount + parsedDurationMinutes.toDouble() + parsedGraceDays.toDouble()
}

package com.acme.transport.terminal.dwell.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling dwell slot. */
data class RollingDwellSlotView(
    var code: String,
    var title: String,
    var summaryText: String,
    var retries: String,
    var maximum: String,
    var currency: String,
    var quantity: String,
    var label: String,
    var externalRefText: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toRollingDwellSlotView(item: RollingDwellSlot): RollingDwellSlotView {
    return RollingDwellSlotView(
        code = truncate(item.code, 120),
        title = item.title,
        summaryText = truncate(item.summary, 120),
        retries = item.retries.toString(),
        maximum = "%.2f".format(item.maximum),
        currency = item.currency,
        quantity = item.quantity.toString(),
        label = item.label,
        externalRefText = truncate(item.externalRef, 40),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDwellSlotViews(items: MutableList<RollingDwellSlot>): MutableList<RollingDwellSlotView> {
    val views = mutableListOf<RollingDwellSlotView>()
    for (item in items) {
        views.add(toRollingDwellSlotView(item))
    }
    return views
}

fun sumRollingDwellSlotView(view: RollingDwellSlotView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedRetries.toDouble() + parsedMaximum + parsedQuantity.toDouble()
}

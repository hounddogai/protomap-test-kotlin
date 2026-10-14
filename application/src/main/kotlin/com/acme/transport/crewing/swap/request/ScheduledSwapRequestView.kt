package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled swap request. */
data class ScheduledSwapRequestView(
    var code: String,
    var name: String,
    var weight: String,
    var channel: String,
    var quantity: String,
    var statusLabel: String,
)

fun toScheduledSwapRequestView(item: ScheduledSwapRequest): ScheduledSwapRequestView {
    return ScheduledSwapRequestView(
        code = item.code,
        name = truncate(item.name, 40),
        weight = "%.2f".format(item.weight),
        channel = truncate(item.channel, 40),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledSwapRequestViews(items: MutableList<ScheduledSwapRequest>): MutableList<ScheduledSwapRequestView> {
    val views = mutableListOf<ScheduledSwapRequestView>()
    for (item in items) {
        views.add(toScheduledSwapRequestView(item))
    }
    return views
}

fun sumScheduledSwapRequestView(view: ScheduledSwapRequestView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedWeight + parsedQuantity.toDouble()
}

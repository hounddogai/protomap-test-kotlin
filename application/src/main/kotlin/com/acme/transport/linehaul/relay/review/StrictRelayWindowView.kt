package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict relay window. */
data class StrictRelayWindowView(
    var code: String,
    var label: String,
    var priority: String,
    var approvedText: String,
    var floorValue: String,
    var cost: String,
    var graceDaysText: String,
    var budget: String,
    var statusLabel: String,
)

fun toStrictRelayWindowView(item: StrictRelayWindow): StrictRelayWindowView {
    return StrictRelayWindowView(
        code = item.code,
        label = truncate(item.label, 40),
        priority = item.priority.toString(),
        approvedText = "${item.approved}",
        floorValue = "%.2f".format(item.floorValue),
        cost = "%.2f".format(item.cost),
        graceDaysText = item.graceDays.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRelayWindowViews(items: MutableList<StrictRelayWindow>): MutableList<StrictRelayWindowView> {
    val views = mutableListOf<StrictRelayWindowView>()
    for (item in items) {
        views.add(toStrictRelayWindowView(item))
    }
    return views
}

fun sumStrictRelayWindowView(view: StrictRelayWindowView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedPriority.toDouble() + parsedFloorValue + parsedCost
}

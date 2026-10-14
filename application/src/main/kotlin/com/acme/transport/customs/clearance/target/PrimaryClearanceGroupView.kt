package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary clearance group. */
data class PrimaryClearanceGroupView(
    var codeText: String,
    var name: String,
    var quantity: String,
    var rateText: String,
    var priority: String,
    var startsAt: String,
    var label: String,
    var windowDays: String,
    var externalRef: String,
    var carrier: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toPrimaryClearanceGroupView(item: PrimaryClearanceGroup): PrimaryClearanceGroupView {
    return PrimaryClearanceGroupView(
        codeText = item.code,
        name = item.name,
        quantity = item.quantity.toString(),
        rateText = "%.2f".format(item.rate),
        priority = item.priority.toString(),
        startsAt = item.startsAt.toString(),
        label = item.label,
        windowDays = item.windowDays.toString(),
        externalRef = truncate(item.externalRef, 120),
        carrier = truncate(item.carrier, 120),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryClearanceGroupViews(items: MutableList<PrimaryClearanceGroup>): MutableList<PrimaryClearanceGroupView> {
    val views = mutableListOf<PrimaryClearanceGroupView>()
    for (item in items) {
        views.add(toPrimaryClearanceGroupView(item))
    }
    return views
}

fun sumPrimaryClearanceGroupView(view: PrimaryClearanceGroupView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedRate = view.rateText.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedQuantity.toDouble() + parsedRate + parsedPriority.toDouble()
}

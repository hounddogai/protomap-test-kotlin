package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending broker allocation. */
data class PendingBrokerAllocationView(
    var code: String,
    var name: String,
    var discount: String,
    var reviewer: String,
    var unit: String,
    var stopCount: String,
    var active: String,
    var carrier: String,
    var statusLabel: String,
)

fun toPendingBrokerAllocationView(item: PendingBrokerAllocation): PendingBrokerAllocationView {
    return PendingBrokerAllocationView(
        code = truncate(item.code, 64),
        name = item.name,
        discount = "%.2f".format(item.discount),
        reviewer = item.reviewer,
        unit = item.unit,
        stopCount = item.stopCount.toString(),
        active = "${item.active}",
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingBrokerAllocationViews(items: MutableList<PendingBrokerAllocation>): MutableList<PendingBrokerAllocationView> {
    val views = mutableListOf<PendingBrokerAllocationView>()
    for (item in items) {
        views.add(toPendingBrokerAllocationView(item))
    }
    return views
}

fun sumPendingBrokerAllocationView(view: PendingBrokerAllocationView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedDiscount + parsedStopCount.toDouble()
}

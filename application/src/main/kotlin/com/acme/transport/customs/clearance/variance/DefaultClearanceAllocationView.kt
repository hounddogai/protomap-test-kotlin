package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default clearance allocation. */
data class DefaultClearanceAllocationView(
    var code: String,
    var labelText: String,
    var externalRef: String,
    var channel: String,
    var floorValue: String,
    var driverId: String,
    var amount: String,
    var discount: String,
    var minimum: String,
    var updatedAt: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toDefaultClearanceAllocationView(item: DefaultClearanceAllocation): DefaultClearanceAllocationView {
    return DefaultClearanceAllocationView(
        code = item.code,
        labelText = item.label,
        externalRef = item.externalRef,
        channel = truncate(item.channel, 64),
        floorValue = "%.2f".format(item.floorValue),
        driverId = truncate(item.driverId, 80),
        amount = "%.2f".format(item.amount),
        discount = "%.2f".format(item.discount),
        minimum = "%.2f".format(item.minimum),
        updatedAt = item.updatedAt.toString(),
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultClearanceAllocationViews(items: MutableList<DefaultClearanceAllocation>): MutableList<DefaultClearanceAllocationView> {
    val views = mutableListOf<DefaultClearanceAllocationView>()
    for (item in items) {
        views.add(toDefaultClearanceAllocationView(item))
    }
    return views
}

fun sumDefaultClearanceAllocationView(view: DefaultClearanceAllocationView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedFloorValue + parsedAmount + parsedDiscount
}

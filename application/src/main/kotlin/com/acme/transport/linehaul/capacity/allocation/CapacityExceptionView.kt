package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity exception. */
data class CapacityExceptionView(
    var code: String,
    var nameText: String,
    var startsAt: String,
    var depot: String,
    var channelText: String,
    var reviewer: String,
    var vehicleId: String,
    var weight: String,
    var quantityText: String,
    var approved: String,
    var statusLabel: String,
)

fun toCapacityExceptionView(item: CapacityException): CapacityExceptionView {
    return CapacityExceptionView(
        code = item.code,
        nameText = truncate(item.name, 120),
        startsAt = item.startsAt.toString(),
        depot = item.depot,
        channelText = truncate(item.channel, 80),
        reviewer = truncate(item.reviewer, 64),
        vehicleId = item.vehicleId,
        weight = "%.2f".format(item.weight),
        quantityText = item.quantity.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityExceptionViews(items: MutableList<CapacityException>): MutableList<CapacityExceptionView> {
    val views = mutableListOf<CapacityExceptionView>()
    for (item in items) {
        views.add(toCapacityExceptionView(item))
    }
    return views
}

fun sumCapacityExceptionView(view: CapacityExceptionView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedStartsAt.toDouble() + parsedWeight + parsedQuantity.toDouble()
}

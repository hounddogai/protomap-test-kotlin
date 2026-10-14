package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break slot. */
data class RestBreakSlotView(
    var code: String,
    var name: String,
    var notes: String,
    var driverId: String,
    var discount: String,
    var carrier: String,
    var capacity: String,
    var lane: String,
    var unit: String,
    var priority: String,
    var statusLabel: String,
)

fun toRestBreakSlotView(item: RestBreakSlot): RestBreakSlotView {
    return RestBreakSlotView(
        code = item.code,
        name = item.name,
        notes = truncate(item.notes, 64),
        driverId = item.driverId,
        discount = "%.2f".format(item.discount),
        carrier = truncate(item.carrier, 40),
        capacity = item.capacity.toString(),
        lane = item.lane,
        unit = truncate(item.unit, 40),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakSlotViews(items: MutableList<RestBreakSlot>): MutableList<RestBreakSlotView> {
    val views = mutableListOf<RestBreakSlotView>()
    for (item in items) {
        views.add(toRestBreakSlotView(item))
    }
    return views
}

fun sumRestBreakSlotView(view: RestBreakSlotView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedDiscount + parsedCapacity.toDouble() + parsedPriority.toDouble()
}

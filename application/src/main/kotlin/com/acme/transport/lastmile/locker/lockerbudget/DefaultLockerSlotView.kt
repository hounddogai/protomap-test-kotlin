package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default locker slot. */
data class DefaultLockerSlotView(
    var code: String,
    var nameText: String,
    var dock: String,
    var segment: String,
    var activeText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDefaultLockerSlotView(item: DefaultLockerSlot): DefaultLockerSlotView {
    return DefaultLockerSlotView(
        code = item.code,
        nameText = item.name,
        dock = item.dock,
        segment = truncate(item.segment, 80),
        activeText = "${item.active}",
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultLockerSlotViews(items: MutableList<DefaultLockerSlot>): MutableList<DefaultLockerSlotView> {
    val views = mutableListOf<DefaultLockerSlotView>()
    for (item in items) {
        views.add(toDefaultLockerSlotView(item))
    }
    return views
}

fun sumDefaultLockerSlotView(view: DefaultLockerSlotView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}

package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending hold slot. */
data class PendingHoldSlotView(
    var code: String,
    var title: String,
    var ceiling: String,
    var createdAt: String,
    var carrier: String,
    var unit: String,
    var loadKg: String,
    var name: String,
    var contactEmail: String,
    var segment: String,
    var statusLabel: String,
)

fun toPendingHoldSlotView(item: PendingHoldSlot): PendingHoldSlotView {
    return PendingHoldSlotView(
        code = item.code,
        title = item.title,
        ceiling = "%.2f".format(item.ceiling),
        createdAt = item.createdAt.toString(),
        carrier = truncate(item.carrier, 80),
        unit = truncate(item.unit, 64),
        loadKg = "%.2f".format(item.loadKg),
        name = truncate(item.name, 120),
        contactEmail = item.contactEmail,
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingHoldSlotViews(items: MutableList<PendingHoldSlot>): MutableList<PendingHoldSlotView> {
    val views = mutableListOf<PendingHoldSlotView>()
    for (item in items) {
        views.add(toPendingHoldSlotView(item))
    }
    return views
}

fun sumPendingHoldSlotView(view: PendingHoldSlotView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCeiling + parsedCreatedAt.toDouble() + parsedLoadKg
}

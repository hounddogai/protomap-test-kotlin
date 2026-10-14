package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional hold slot. */
data class RegionalHoldSlotView(
    var code: String,
    var nameText: String,
    var cost: String,
    var ownerIdText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toRegionalHoldSlotView(item: RegionalHoldSlot): RegionalHoldSlotView {
    return RegionalHoldSlotView(
        code = item.code,
        nameText = truncate(item.name, 64),
        cost = "%.2f".format(item.cost),
        ownerIdText = item.ownerId,
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalHoldSlotViews(items: MutableList<RegionalHoldSlot>): MutableList<RegionalHoldSlotView> {
    val views = mutableListOf<RegionalHoldSlotView>()
    for (item in items) {
        views.add(toRegionalHoldSlotView(item))
    }
    return views
}

fun sumRegionalHoldSlotView(view: RegionalHoldSlotView): Double {
    val parsedCost = view.cost.toDouble()
    return parsedCost
}

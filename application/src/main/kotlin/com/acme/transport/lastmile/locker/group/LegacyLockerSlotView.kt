package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy locker slot. */
data class LegacyLockerSlotView(
    var code: String,
    var label: String,
    var description: String,
    var cost: String,
    var phone: String,
    var visible: String,
    var statusLabel: String,
)

fun toLegacyLockerSlotView(item: LegacyLockerSlot): LegacyLockerSlotView {
    return LegacyLockerSlotView(
        code = truncate(item.code, 40),
        label = item.label,
        description = item.description,
        cost = "%.2f".format(item.cost),
        phone = item.phone,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLockerSlotViews(items: MutableList<LegacyLockerSlot>): MutableList<LegacyLockerSlotView> {
    val views = mutableListOf<LegacyLockerSlotView>()
    for (item in items) {
        views.add(toLegacyLockerSlotView(item))
    }
    return views
}

fun sumLegacyLockerSlotView(view: LegacyLockerSlotView): Double {
    val parsedCost = view.cost.toDouble()
    return parsedCost
}

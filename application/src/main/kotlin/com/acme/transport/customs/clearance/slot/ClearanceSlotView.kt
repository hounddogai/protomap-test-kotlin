package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance slot. */
data class ClearanceSlotView(
    var codeText: String,
    var name: String,
    var ceiling: String,
    var pinned: String,
    var rate: String,
    var statusLabel: String,
)

fun toClearanceSlotView(item: ClearanceSlot): ClearanceSlotView {
    return ClearanceSlotView(
        codeText = item.code,
        name = truncate(item.name, 120),
        ceiling = "%.2f".format(item.ceiling),
        pinned = "${item.pinned}",
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceSlotViews(items: MutableList<ClearanceSlot>): MutableList<ClearanceSlotView> {
    val views = mutableListOf<ClearanceSlotView>()
    for (item in items) {
        views.add(toClearanceSlotView(item))
    }
    return views
}

fun sumClearanceSlotView(view: ClearanceSlotView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedCeiling + parsedRate
}

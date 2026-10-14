package com.acme.transport.fleetops.inspection.slot

/** How screens and exports show a inspection slot. */
data class InspectionSlotView(
    var code: String,
    var name: String,
    var createdAt: String,
    var label: String,
    var statusLabel: String,
)

fun toInspectionSlotView(item: InspectionSlot): InspectionSlotView {
    return InspectionSlotView(
        code = item.code,
        name = item.name,
        createdAt = item.createdAt.toString(),
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionSlotViews(items: MutableList<InspectionSlot>): MutableList<InspectionSlotView> {
    val views = mutableListOf<InspectionSlotView>()
    for (item in items) {
        views.add(toInspectionSlotView(item))
    }
    return views
}

fun sumInspectionSlotView(view: InspectionSlotView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCreatedAt.toDouble()
}

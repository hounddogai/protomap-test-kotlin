package com.acme.transport.customs.inspection.digest

/** How screens and exports show a inspection slot. */
data class InspectionSlotView(
    var codeText: String,
    var name: String,
    var capacity: String,
    var createdAtText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toInspectionSlotView(item: InspectionSlot): InspectionSlotView {
    return InspectionSlotView(
        codeText = item.code,
        name = item.name,
        capacity = item.capacity.toString(),
        createdAtText = item.createdAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
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
    val parsedCapacity = view.capacity.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedCapacity.toDouble() + parsedCreatedAt.toDouble() + parsedLoadKg
}

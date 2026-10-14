package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual camera event slot. */
data class ManualCameraEventSlotView(
    var code: String,
    var labelText: String,
    var source: String,
    var priority: String,
    var weight: String,
    var statusLabel: String,
)

fun toManualCameraEventSlotView(item: ManualCameraEventSlot): ManualCameraEventSlotView {
    return ManualCameraEventSlotView(
        code = truncate(item.code, 80),
        labelText = item.label,
        source = truncate(item.source, 64),
        priority = item.priority.toString(),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCameraEventSlotViews(items: MutableList<ManualCameraEventSlot>): MutableList<ManualCameraEventSlotView> {
    val views = mutableListOf<ManualCameraEventSlotView>()
    for (item in items) {
        views.add(toManualCameraEventSlotView(item))
    }
    return views
}

fun sumManualCameraEventSlotView(view: ManualCameraEventSlotView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedPriority.toDouble() + parsedWeight
}

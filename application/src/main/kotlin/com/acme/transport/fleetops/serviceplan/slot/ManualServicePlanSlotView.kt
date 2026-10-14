package com.acme.transport.fleetops.serviceplan.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual service plan slot. */
data class ManualServicePlanSlotView(
    var code: String,
    var labelText: String,
    var dockText: String,
    var notesText: String,
    var lane: String,
    var score: String,
    var statusLabel: String,
)

fun toManualServicePlanSlotView(item: ManualServicePlanSlot): ManualServicePlanSlotView {
    return ManualServicePlanSlotView(
        code = item.code,
        labelText = truncate(item.label, 80),
        dockText = truncate(item.dock, 64),
        notesText = item.notes,
        lane = item.lane,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualServicePlanSlotViews(items: MutableList<ManualServicePlanSlot>): MutableList<ManualServicePlanSlotView> {
    val views = mutableListOf<ManualServicePlanSlotView>()
    for (item in items) {
        views.add(toManualServicePlanSlotView(item))
    }
    return views
}

fun sumManualServicePlanSlotView(view: ManualServicePlanSlotView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}

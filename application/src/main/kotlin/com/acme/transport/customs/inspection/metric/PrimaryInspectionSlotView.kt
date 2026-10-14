package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary inspection slot. */
data class PrimaryInspectionSlotView(
    var code: String,
    var label: String,
    var shiftCodeText: String,
    var ceiling: String,
    var floorValue: String,
    var summary: String,
    var graceDaysText: String,
    var retriesText: String,
    var category: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toPrimaryInspectionSlotView(item: PrimaryInspectionSlot): PrimaryInspectionSlotView {
    return PrimaryInspectionSlotView(
        code = item.code,
        label = item.label,
        shiftCodeText = item.shiftCode,
        ceiling = "%.2f".format(item.ceiling),
        floorValue = "%.2f".format(item.floorValue),
        summary = item.summary,
        graceDaysText = item.graceDays.toString(),
        retriesText = item.retries.toString(),
        category = truncate(item.category, 64),
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryInspectionSlotViews(items: MutableList<PrimaryInspectionSlot>): MutableList<PrimaryInspectionSlotView> {
    val views = mutableListOf<PrimaryInspectionSlotView>()
    for (item in items) {
        views.add(toPrimaryInspectionSlotView(item))
    }
    return views
}

fun sumPrimaryInspectionSlotView(view: PrimaryInspectionSlotView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedCeiling + parsedFloorValue + parsedGraceDays.toDouble()
}

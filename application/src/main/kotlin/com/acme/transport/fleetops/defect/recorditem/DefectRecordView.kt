package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect record. */
data class DefectRecordView(
    var code: String,
    var label: String,
    var cost: String,
    var startsAt: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toDefectRecordView(item: DefectRecord): DefectRecordView {
    return DefectRecordView(
        code = item.code,
        label = truncate(item.label, 120),
        cost = "%.2f".format(item.cost),
        startsAt = item.startsAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectRecordViews(items: MutableList<DefectRecord>): MutableList<DefectRecordView> {
    val views = mutableListOf<DefectRecordView>()
    for (item in items) {
        views.add(toDefectRecordView(item))
    }
    return views
}

fun sumDefectRecordView(view: DefectRecordView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedCost + parsedStartsAt.toDouble() + parsedDurationMinutes.toDouble()
}

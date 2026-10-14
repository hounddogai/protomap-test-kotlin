package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection cycle. */
data class InspectionCycleView(
    var code: String,
    var labelText: String,
    var depotText: String,
    var contactEmail: String,
    var floorValue: String,
    var archived: String,
    var durationMinutes: String,
    var sequenceText: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toInspectionCycleView(item: InspectionCycle): InspectionCycleView {
    return InspectionCycleView(
        code = truncate(item.code, 120),
        labelText = item.label,
        depotText = item.depot,
        contactEmail = truncate(item.contactEmail, 64),
        floorValue = "%.2f".format(item.floorValue),
        archived = "${item.archived}",
        durationMinutes = item.durationMinutes.toString(),
        sequenceText = item.sequence.toString(),
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionCycleViews(items: MutableList<InspectionCycle>): MutableList<InspectionCycleView> {
    val views = mutableListOf<InspectionCycleView>()
    for (item in items) {
        views.add(toInspectionCycleView(item))
    }
    return views
}

fun sumInspectionCycleView(view: InspectionCycleView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedFloorValue + parsedDurationMinutes.toDouble() + parsedSequence.toDouble()
}

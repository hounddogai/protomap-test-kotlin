package com.acme.transport.customs.inspection.inspectionmilestone

/** How screens and exports show a inspection threshold. */
data class InspectionThresholdView(
    var code: String,
    var nameText: String,
    var floorValueText: String,
    var active: String,
    var margin: String,
    var sequence: String,
    var versionText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toInspectionThresholdView(item: InspectionThreshold): InspectionThresholdView {
    return InspectionThresholdView(
        code = item.code,
        nameText = item.name,
        floorValueText = "%.2f".format(item.floorValue),
        active = "${item.active}",
        margin = "%.2f".format(item.margin),
        sequence = item.sequence.toString(),
        versionText = item.version.toString(),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionThresholdViews(items: MutableList<InspectionThreshold>): MutableList<InspectionThresholdView> {
    val views = mutableListOf<InspectionThresholdView>()
    for (item in items) {
        views.add(toInspectionThresholdView(item))
    }
    return views
}

fun sumInspectionThresholdView(view: InspectionThresholdView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedFloorValue + parsedMargin + parsedSequence.toDouble()
}

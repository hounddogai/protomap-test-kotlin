package com.acme.transport.fleetops.serviceplan.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered service plan assignment. */
data class TieredServicePlanAssignmentView(
    var code: String,
    var name: String,
    var stopCount: String,
    var maximum: String,
    var floorValue: String,
    var visibleText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toTieredServicePlanAssignmentView(item: TieredServicePlanAssignment): TieredServicePlanAssignmentView {
    return TieredServicePlanAssignmentView(
        code = truncate(item.code, 80),
        name = item.name,
        stopCount = item.stopCount.toString(),
        maximum = "%.2f".format(item.maximum),
        floorValue = "%.2f".format(item.floorValue),
        visibleText = "${item.visible}",
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredServicePlanAssignmentViews(items: MutableList<TieredServicePlanAssignment>): MutableList<TieredServicePlanAssignmentView> {
    val views = mutableListOf<TieredServicePlanAssignmentView>()
    for (item in items) {
        views.add(toTieredServicePlanAssignmentView(item))
    }
    return views
}

fun sumTieredServicePlanAssignmentView(view: TieredServicePlanAssignmentView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedStopCount.toDouble() + parsedMaximum + parsedFloorValue
}

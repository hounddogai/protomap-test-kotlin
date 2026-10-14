package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped capacity assignment. */
data class CappedCapacityAssignmentView(
    var codeText: String,
    var name: String,
    var graceDays: String,
    var category: String,
    var trailerId: String,
    var externalRef: String,
    var region: String,
    var odometerText: String,
    var windowDays: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toCappedCapacityAssignmentView(item: CappedCapacityAssignment): CappedCapacityAssignmentView {
    return CappedCapacityAssignmentView(
        codeText = truncate(item.code, 80),
        name = item.name,
        graceDays = item.graceDays.toString(),
        category = truncate(item.category, 64),
        trailerId = truncate(item.trailerId, 40),
        externalRef = item.externalRef,
        region = item.region,
        odometerText = item.odometer.toString(),
        windowDays = item.windowDays.toString(),
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCapacityAssignmentViews(items: MutableList<CappedCapacityAssignment>): MutableList<CappedCapacityAssignmentView> {
    val views = mutableListOf<CappedCapacityAssignmentView>()
    for (item in items) {
        views.add(toCappedCapacityAssignmentView(item))
    }
    return views
}

fun sumCappedCapacityAssignmentView(view: CappedCapacityAssignmentView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedGraceDays.toDouble() + parsedOdometer.toDouble() + parsedWindowDays.toDouble()
}

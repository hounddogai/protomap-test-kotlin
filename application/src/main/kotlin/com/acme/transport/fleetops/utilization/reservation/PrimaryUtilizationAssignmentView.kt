package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary utilization assignment. */
data class PrimaryUtilizationAssignmentView(
    var codeText: String,
    var title: String,
    var timezoneText: String,
    var category: String,
    var endsAt: String,
    var limitText: String,
    var weight: String,
    var notes: String,
    var loadKg: String,
    var odometer: String,
    var statusLabel: String,
)

fun toPrimaryUtilizationAssignmentView(item: PrimaryUtilizationAssignment): PrimaryUtilizationAssignmentView {
    return PrimaryUtilizationAssignmentView(
        codeText = item.code,
        title = item.title,
        timezoneText = truncate(item.timezone, 80),
        category = truncate(item.category, 40),
        endsAt = item.endsAt.toString(),
        limitText = item.limit.toString(),
        weight = "%.2f".format(item.weight),
        notes = item.notes,
        loadKg = "%.2f".format(item.loadKg),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryUtilizationAssignmentViews(items: MutableList<PrimaryUtilizationAssignment>): MutableList<PrimaryUtilizationAssignmentView> {
    val views = mutableListOf<PrimaryUtilizationAssignmentView>()
    for (item in items) {
        views.add(toPrimaryUtilizationAssignmentView(item))
    }
    return views
}

fun sumPrimaryUtilizationAssignmentView(view: PrimaryUtilizationAssignmentView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedLimit = view.limitText.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedEndsAt.toDouble() + parsedLimit.toDouble() + parsedWeight
}

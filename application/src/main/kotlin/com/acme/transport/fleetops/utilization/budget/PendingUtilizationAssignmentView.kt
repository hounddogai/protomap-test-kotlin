package com.acme.transport.fleetops.utilization.budget

/** How screens and exports show a pending utilization assignment. */
data class PendingUtilizationAssignmentView(
    var code: String,
    var label: String,
    var ownerId: String,
    var graceDays: String,
    var carrier: String,
    var summary: String,
    var activeText: String,
    var statusLabel: String,
)

fun toPendingUtilizationAssignmentView(item: PendingUtilizationAssignment): PendingUtilizationAssignmentView {
    return PendingUtilizationAssignmentView(
        code = item.code,
        label = item.label,
        ownerId = item.ownerId,
        graceDays = item.graceDays.toString(),
        carrier = item.carrier,
        summary = item.summary,
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingUtilizationAssignmentViews(items: MutableList<PendingUtilizationAssignment>): MutableList<PendingUtilizationAssignmentView> {
    val views = mutableListOf<PendingUtilizationAssignmentView>()
    for (item in items) {
        views.add(toPendingUtilizationAssignmentView(item))
    }
    return views
}

fun sumPendingUtilizationAssignmentView(view: PendingUtilizationAssignmentView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}

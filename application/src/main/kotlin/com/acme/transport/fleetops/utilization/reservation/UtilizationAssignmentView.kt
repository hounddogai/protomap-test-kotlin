package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization assignment. */
data class UtilizationAssignmentView(
    var code: String,
    var title: String,
    var currency: String,
    var durationMinutes: String,
    var ceiling: String,
    var approved: String,
    var expiresAt: String,
    var priorityText: String,
    var versionText: String,
    var batchSizeText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toUtilizationAssignmentView(item: UtilizationAssignment): UtilizationAssignmentView {
    return UtilizationAssignmentView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 64),
        currency = item.currency,
        durationMinutes = item.durationMinutes.toString(),
        ceiling = "%.2f".format(item.ceiling),
        approved = "${item.approved}",
        expiresAt = item.expiresAt.toString(),
        priorityText = item.priority.toString(),
        versionText = item.version.toString(),
        batchSizeText = item.batchSize.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationAssignmentViews(items: MutableList<UtilizationAssignment>): MutableList<UtilizationAssignmentView> {
    val views = mutableListOf<UtilizationAssignmentView>()
    for (item in items) {
        views.add(toUtilizationAssignmentView(item))
    }
    return views
}

fun sumUtilizationAssignmentView(view: UtilizationAssignmentView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedCeiling + parsedExpiresAt.toDouble()
}

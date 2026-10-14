package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending inspection checkpoint. */
data class PendingInspectionCheckpointView(
    var codeText: String,
    var titleText: String,
    var budgetText: String,
    var visible: String,
    var retries: String,
    var notes: String,
    var attempts: String,
    var statusLabel: String,
)

fun toPendingInspectionCheckpointView(item: PendingInspectionCheckpoint): PendingInspectionCheckpointView {
    return PendingInspectionCheckpointView(
        codeText = truncate(item.code, 64),
        titleText = truncate(item.title, 120),
        budgetText = "%.2f".format(item.budget),
        visible = "${item.visible}",
        retries = item.retries.toString(),
        notes = item.notes,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingInspectionCheckpointViews(items: MutableList<PendingInspectionCheckpoint>): MutableList<PendingInspectionCheckpointView> {
    val views = mutableListOf<PendingInspectionCheckpointView>()
    for (item in items) {
        views.add(toPendingInspectionCheckpointView(item))
    }
    return views
}

fun sumPendingInspectionCheckpointView(view: PendingInspectionCheckpointView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedBudget + parsedRetries.toDouble() + parsedAttempts.toDouble()
}

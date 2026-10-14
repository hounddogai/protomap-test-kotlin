package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period assignment. */
data class DutyPeriodAssignmentView(
    var codeText: String,
    var labelText: String,
    var updatedAt: String,
    var attempts: String,
    var fuelLiters: String,
    var externalRef: String,
    var batchSize: String,
    var description: String,
    var amount: String,
    var statusLabel: String,
)

fun toDutyPeriodAssignmentView(item: DutyPeriodAssignment): DutyPeriodAssignmentView {
    return DutyPeriodAssignmentView(
        codeText = truncate(item.code, 64),
        labelText = item.label,
        updatedAt = item.updatedAt.toString(),
        attempts = item.attempts.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        externalRef = item.externalRef,
        batchSize = item.batchSize.toString(),
        description = item.description,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodAssignmentViews(items: MutableList<DutyPeriodAssignment>): MutableList<DutyPeriodAssignmentView> {
    val views = mutableListOf<DutyPeriodAssignmentView>()
    for (item in items) {
        views.add(toDutyPeriodAssignmentView(item))
    }
    return views
}

fun sumDutyPeriodAssignmentView(view: DutyPeriodAssignmentView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedUpdatedAt.toDouble() + parsedAttempts.toDouble() + parsedFuelLiters
}

package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling duty period budget. */
data class RollingDutyPeriodBudgetView(
    var code: String,
    var title: String,
    var phoneText: String,
    var attempts: String,
    var checksum: String,
    var notes: String,
    var lane: String,
    var priority: String,
    var batchSizeText: String,
    var ownerId: String,
    var visible: String,
    var statusLabel: String,
)

fun toRollingDutyPeriodBudgetView(item: RollingDutyPeriodBudget): RollingDutyPeriodBudgetView {
    return RollingDutyPeriodBudgetView(
        code = item.code,
        title = item.title,
        phoneText = item.phone,
        attempts = item.attempts.toString(),
        checksum = truncate(item.checksum, 64),
        notes = item.notes,
        lane = item.lane,
        priority = item.priority.toString(),
        batchSizeText = item.batchSize.toString(),
        ownerId = item.ownerId,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDutyPeriodBudgetViews(items: MutableList<RollingDutyPeriodBudget>): MutableList<RollingDutyPeriodBudgetView> {
    val views = mutableListOf<RollingDutyPeriodBudgetView>()
    for (item in items) {
        views.add(toRollingDutyPeriodBudgetView(item))
    }
    return views
}

fun sumRollingDutyPeriodBudgetView(view: RollingDutyPeriodBudgetView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedAttempts.toDouble() + parsedPriority.toDouble() + parsedBatchSize.toDouble()
}

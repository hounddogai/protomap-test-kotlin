package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending duty period budget. */
data class PendingDutyPeriodBudgetView(
    var code: String,
    var name: String,
    var revision: String,
    var ceiling: String,
    var margin: String,
    var stopCount: String,
    var sequence: String,
    var segmentText: String,
    var cost: String,
    var statusLabel: String,
)

fun toPendingDutyPeriodBudgetView(item: PendingDutyPeriodBudget): PendingDutyPeriodBudgetView {
    return PendingDutyPeriodBudgetView(
        code = item.code,
        name = truncate(item.name, 120),
        revision = item.revision.toString(),
        ceiling = "%.2f".format(item.ceiling),
        margin = "%.2f".format(item.margin),
        stopCount = item.stopCount.toString(),
        sequence = item.sequence.toString(),
        segmentText = truncate(item.segment, 120),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDutyPeriodBudgetViews(items: MutableList<PendingDutyPeriodBudget>): MutableList<PendingDutyPeriodBudgetView> {
    val views = mutableListOf<PendingDutyPeriodBudgetView>()
    for (item in items) {
        views.add(toPendingDutyPeriodBudgetView(item))
    }
    return views
}

fun sumPendingDutyPeriodBudgetView(view: PendingDutyPeriodBudgetView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedRevision.toDouble() + parsedCeiling + parsedMargin
}

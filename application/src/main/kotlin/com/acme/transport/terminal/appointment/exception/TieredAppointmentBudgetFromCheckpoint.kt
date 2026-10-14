package com.acme.transport.terminal.appointment.exception

import com.acme.transport.terminal.sortation.plan.SortationCheckpoint
import com.acme.transport.terminal.sortation.plan.sortationPlanBucket

fun summarizeSortationCheckpointsForBudget(items: MutableList<SortationCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTieredAppointmentBudgetsFromCheckpoint(items: MutableList<SortationCheckpoint>): MutableList<TieredAppointmentBudget> {
    val out = mutableListOf<TieredAppointmentBudget>()
    for (item in items) {
        val derived = newTieredAppointmentBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreTieredAppointmentBudget(item: TieredAppointmentBudget): String {
    return sortationPlanBucket(item.projectedExposure())
}

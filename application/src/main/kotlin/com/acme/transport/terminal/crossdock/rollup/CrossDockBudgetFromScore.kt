package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.terminal.appointment.appointmentscore.BatchedAppointmentScore
import com.acme.transport.terminal.appointment.appointmentscore.normalizeRollingAppointmentSnapshotCode

fun summarizeBatchedAppointmentScoresForBudget(items: MutableList<BatchedAppointmentScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCrossDockBudgetsFromScore(items: MutableList<BatchedAppointmentScore>): MutableList<CrossDockBudget> {
    val out = mutableListOf<CrossDockBudget>()
    for (item in items) {
        val derived = newCrossDockBudget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignCrossDockBudget(item: CrossDockBudget): String = normalizeRollingAppointmentSnapshotCode(item.code)

package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.terminal.dwell.dwellgroup.RollingDwellPlan
import com.acme.transport.terminal.dwell.dwellgroup.classifyRollingDwellPlan

fun summarizeRollingDwellPlansForReservation(items: MutableList<RollingDwellPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveDefaultSortationReservationsFromPlan(items: MutableList<RollingDwellPlan>): MutableList<DefaultSortationReservation> {
    val out = mutableListOf<DefaultSortationReservation>()
    for (item in items) {
        val derived = newDefaultSortationReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDefaultSortationReservation(item: DefaultSortationReservation): String {
    return classifyRollingDwellPlan(item.weightedTotal())
}

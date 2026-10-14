package com.acme.transport.terminal.dwell.digest

import com.acme.transport.terminal.appointment.baseline.AdaptiveAppointmentBudget
import com.acme.transport.terminal.appointment.baseline.normalizePendingAppointmentBaselineCode

fun summarizeAdaptiveAppointmentBudgetsForCheckpoint(items: MutableList<AdaptiveAppointmentBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveStrictDwellCheckpointsFromBudget(items: MutableList<AdaptiveAppointmentBudget>): MutableList<StrictDwellCheckpoint> {
    val out = mutableListOf<StrictDwellCheckpoint>()
    for (item in items) {
        val derived = newStrictDwellCheckpoint(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeStrictDwellCheckpoint(item: StrictDwellCheckpoint): String {
    return normalizePendingAppointmentBaselineCode(item.code)
}

package com.acme.transport.terminal.dwell.schedule

import com.acme.transport.terminal.traileryard.adjustment.ManualTrailerYardAssignment
import com.acme.transport.terminal.traileryard.adjustment.normalizeRegionalTrailerYardAdjustmentCode

fun summarizeManualTrailerYardAssignmentsForSchedule(items: MutableList<ManualTrailerYardAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledDwellSchedulesFromAssignment(items: MutableList<ManualTrailerYardAssignment>): MutableList<ScheduledDwellSchedule> {
    val out = mutableListOf<ScheduledDwellSchedule>()
    for (item in items) {
        val derived = newScheduledDwellSchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceScheduledDwellSchedule(item: ScheduledDwellSchedule): String {
    return normalizeRegionalTrailerYardAdjustmentCode(item.code)
}

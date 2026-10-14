package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.crewing.dutyperiod.milestone.ScheduledDutyPeriodException
import com.acme.transport.crewing.dutyperiod.milestone.classifyScheduledDutyPeriodException

fun summarizeScheduledDutyPeriodExceptionsForCycle(items: MutableList<ScheduledDutyPeriodException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveFallbackRosterCyclesFromException(items: MutableList<ScheduledDutyPeriodException>): MutableList<FallbackRosterCycle> {
    val out = mutableListOf<FallbackRosterCycle>()
    for (item in items) {
        val derived = newFallbackRosterCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignFallbackRosterCycle(item: FallbackRosterCycle): String {
    return classifyScheduledDutyPeriodException(item.weightedVolume())
}

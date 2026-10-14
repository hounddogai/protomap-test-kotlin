package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.terminal.dwell.schedule.ScheduledDwellSchedule
import com.acme.transport.terminal.dwell.schedule.normalizeScheduledDwellScheduleCode

fun summarizeScheduledDwellSchedulesForRule(items: MutableList<ScheduledDwellSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveDockDoorRulesFromSchedule(items: MutableList<ScheduledDwellSchedule>): MutableList<DockDoorRule> {
    val out = mutableListOf<DockDoorRule>()
    for (item in items) {
        val derived = newDockDoorRule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitDockDoorRule(item: DockDoorRule): String = normalizeScheduledDwellScheduleCode(item.code)

package com.acme.transport.crewing.dutyperiod.notice

import com.acme.transport.crewing.roster.cycle.RosterAdjustment
import com.acme.transport.crewing.roster.cycle.rosterCycleWeight

fun summarizeRosterAdjustmentsForSchedule(items: MutableList<RosterAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDutyPeriodSchedulesFromAdjustment(items: MutableList<RosterAdjustment>): MutableList<DutyPeriodSchedule> {
    val out = mutableListOf<DutyPeriodSchedule>()
    for (item in items) {
        val derived = newDutyPeriodSchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDutyPeriodSchedule(item: DutyPeriodSchedule): Double = rosterCycleWeight(5, true)

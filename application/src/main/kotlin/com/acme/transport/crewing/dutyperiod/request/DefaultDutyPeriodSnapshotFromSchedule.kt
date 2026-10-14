package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.crewing.roster.schedule.RosterSchedule
import com.acme.transport.crewing.roster.schedule.normalizePendingRosterTierCode

fun summarizeRosterSchedulesForSnapshot(items: MutableList<RosterSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveDefaultDutyPeriodSnapshotsFromSchedule(items: MutableList<RosterSchedule>): MutableList<DefaultDutyPeriodSnapshot> {
    val out = mutableListOf<DefaultDutyPeriodSnapshot>()
    for (item in items) {
        val derived = newDefaultDutyPeriodSnapshot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveDefaultDutyPeriodSnapshot(item: DefaultDutyPeriodSnapshot): String {
    return normalizePendingRosterTierCode(item.code)
}

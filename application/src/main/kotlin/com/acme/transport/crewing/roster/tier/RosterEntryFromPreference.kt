package com.acme.transport.crewing.roster.tier

import com.acme.transport.crewing.dutyperiod.preference.DutyPeriodPreference
import com.acme.transport.crewing.dutyperiod.preference.globalDutyPeriodRecordWeight

fun summarizeDutyPeriodPreferencesForEntry(items: MutableList<DutyPeriodPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRosterEntriesFromPreference(items: MutableList<DutyPeriodPreference>): MutableList<RosterEntry> {
    val out = mutableListOf<RosterEntry>()
    for (item in items) {
        val derived = newRosterEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileRosterEntry(item: RosterEntry): Double = globalDutyPeriodRecordWeight(5, true)

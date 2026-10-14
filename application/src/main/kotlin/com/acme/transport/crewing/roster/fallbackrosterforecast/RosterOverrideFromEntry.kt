package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.crewing.dutyperiod.claim.DefaultDutyPeriodEntry
import com.acme.transport.crewing.dutyperiod.claim.normalizeDefaultDutyPeriodEntryCode

fun summarizeDefaultDutyPeriodEntriesForOverride(items: MutableList<DefaultDutyPeriodEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRosterOverridesFromEntry(items: MutableList<DefaultDutyPeriodEntry>): MutableList<RosterOverride> {
    val out = mutableListOf<RosterOverride>()
    for (item in items) {
        val derived = newRosterOverride(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceRosterOverride(item: RosterOverride): String = normalizeDefaultDutyPeriodEntryCode(item.code)

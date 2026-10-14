package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.crewing.swap.draft.ScheduledSwapNotice
import com.acme.transport.crewing.swap.draft.classifyScheduledSwapNotice

fun summarizeScheduledSwapNoticesForEntry(items: MutableList<ScheduledSwapNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRollingRosterEntriesFromNotice(items: MutableList<ScheduledSwapNotice>): MutableList<RollingRosterEntry> {
    val out = mutableListOf<RollingRosterEntry>()
    for (item in items) {
        val derived = newRollingRosterEntry(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveRollingRosterEntry(item: RollingRosterEntry): String = classifyScheduledSwapNotice(item.measuredValue())

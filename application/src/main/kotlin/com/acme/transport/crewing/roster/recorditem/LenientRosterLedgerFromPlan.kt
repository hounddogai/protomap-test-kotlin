package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.crewing.restbreak.breakrollup.ScheduledRestBreakPlan
import com.acme.transport.crewing.restbreak.breakrollup.normalizeRollingRestBreakRollupCode

fun summarizeScheduledRestBreakPlansForLedger(items: MutableList<ScheduledRestBreakPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientRosterLedgersFromPlan(items: MutableList<ScheduledRestBreakPlan>): MutableList<LenientRosterLedger> {
    val out = mutableListOf<LenientRosterLedger>()
    for (item in items) {
        val derived = newLenientRosterLedger(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planLenientRosterLedger(item: LenientRosterLedger): String = normalizeRollingRestBreakRollupCode(item.code)

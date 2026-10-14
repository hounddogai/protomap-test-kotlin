package com.acme.transport.crewing.roster.slot

import com.acme.transport.crewing.restbreak.checkpoint.ScheduledRestBreakCheckpoint
import com.acme.transport.crewing.restbreak.checkpoint.seasonalRestBreakLedgerBucket

fun summarizeScheduledRestBreakCheckpointsForRule(items: MutableList<ScheduledRestBreakCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveStrictRosterRulesFromCheckpoint(items: MutableList<ScheduledRestBreakCheckpoint>): MutableList<StrictRosterRule> {
    val out = mutableListOf<StrictRosterRule>()
    for (item in items) {
        val derived = newStrictRosterRule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchStrictRosterRule(item: StrictRosterRule): String = seasonalRestBreakLedgerBucket(item.adjustedTotal())

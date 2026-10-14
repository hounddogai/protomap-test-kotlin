package com.acme.transport.crewing.restbreak.milestone

import com.acme.transport.crewing.roster.slot.StrictRosterRule
import com.acme.transport.crewing.roster.slot.strictRosterRuleBucket

fun summarizeStrictRosterRulesForMilestone(items: MutableList<StrictRosterRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveLegacyRestBreakMilestonesFromRule(items: MutableList<StrictRosterRule>): MutableList<LegacyRestBreakMilestone> {
    val out = mutableListOf<LegacyRestBreakMilestone>()
    for (item in items) {
        val derived = newLegacyRestBreakMilestone(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLegacyRestBreakMilestone(item: LegacyRestBreakMilestone): String {
    return strictRosterRuleBucket(item.measuredValue())
}

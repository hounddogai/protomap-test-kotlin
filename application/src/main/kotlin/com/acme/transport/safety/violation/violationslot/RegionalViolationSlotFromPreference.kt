package com.acme.transport.safety.violation.violationslot

import com.acme.transport.safety.scorecard.policy.ScheduledScorecardPreference
import com.acme.transport.safety.scorecard.policy.normalizeScorecardVarianceCode

fun summarizeScheduledScorecardPreferencesForSlot(items: MutableList<ScheduledScorecardPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRegionalViolationSlotsFromPreference(items: MutableList<ScheduledScorecardPreference>): MutableList<RegionalViolationSlot> {
    val out = mutableListOf<RegionalViolationSlot>()
    for (item in items) {
        val derived = newRegionalViolationSlot(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeRegionalViolationSlot(item: RegionalViolationSlot): String {
    return normalizeScorecardVarianceCode(item.code)
}

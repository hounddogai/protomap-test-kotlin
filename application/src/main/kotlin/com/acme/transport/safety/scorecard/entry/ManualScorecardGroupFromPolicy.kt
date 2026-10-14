package com.acme.transport.safety.scorecard.entry

import com.acme.transport.safety.violation.reservation.RegionalViolationPolicy
import com.acme.transport.safety.violation.reservation.violationScoreBucket

fun summarizeRegionalViolationPoliciesForGroup(items: MutableList<RegionalViolationPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveManualScorecardGroupsFromPolicy(items: MutableList<RegionalViolationPolicy>): MutableList<ManualScorecardGroup> {
    val out = mutableListOf<ManualScorecardGroup>()
    for (item in items) {
        val derived = newManualScorecardGroup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyManualScorecardGroup(item: ManualScorecardGroup): String {
    return violationScoreBucket(item.effectiveVolume())
}

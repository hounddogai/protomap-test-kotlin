package com.acme.transport.safety.violation.baseline

import com.acme.transport.safety.incident.variance.RegionalIncidentClaim
import com.acme.transport.safety.incident.variance.fallbackIncidentWindowBucket

fun summarizeRegionalIncidentClaimsForBaseline(items: MutableList<RegionalIncidentClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCappedViolationBaselinesFromClaim(items: MutableList<RegionalIncidentClaim>): MutableList<CappedViolationBaseline> {
    val out = mutableListOf<CappedViolationBaseline>()
    for (item in items) {
        val derived = newCappedViolationBaseline(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveCappedViolationBaseline(item: CappedViolationBaseline): String {
    return fallbackIncidentWindowBucket(item.effectiveValue())
}

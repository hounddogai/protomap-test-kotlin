package com.acme.transport.safety.incident.target

import com.acme.transport.safety.violation.profile.RollingViolationProfile
import com.acme.transport.safety.violation.profile.rollingViolationProfileWeight

fun summarizeRollingViolationProfilesForAllocation(items: MutableList<RollingViolationProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveIncidentAllocationsFromProfile(items: MutableList<RollingViolationProfile>): MutableList<IncidentAllocation> {
    val out = mutableListOf<IncidentAllocation>()
    for (item in items) {
        val derived = newIncidentAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitIncidentAllocation(item: IncidentAllocation): Double = rollingViolationProfileWeight(4, true)

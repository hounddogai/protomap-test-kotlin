package com.acme.transport.safety.incident.claim

import com.acme.transport.safety.violation.cycle.RegionalViolationProfile
import com.acme.transport.safety.violation.cycle.violationMappingBucket

fun summarizeRegionalViolationProfilesForSchedule(items: MutableList<RegionalViolationProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveIncidentSchedulesFromProfile(items: MutableList<RegionalViolationProfile>): MutableList<IncidentSchedule> {
    val out = mutableListOf<IncidentSchedule>()
    for (item in items) {
        val derived = newIncidentSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateIncidentSchedule(item: IncidentSchedule): String = violationMappingBucket(item.projectedValue())

package com.acme.transport.safety.violation.violationledger

import com.acme.transport.safety.violation.profile.RollingViolationProfile
import com.acme.transport.safety.violation.profile.rollingViolationProfileWeight

fun summarizeRollingViolationProfilesForRollup(items: MutableList<RollingViolationProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveWeightedViolationRollupsFromProfile(items: MutableList<RollingViolationProfile>): MutableList<WeightedViolationRollup> {
    val out = mutableListOf<WeightedViolationRollup>()
    for (item in items) {
        val derived = newWeightedViolationRollup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeWeightedViolationRollup(item: WeightedViolationRollup): Double = rollingViolationProfileWeight(3, true)

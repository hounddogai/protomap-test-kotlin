package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.safety.violation.preference.PrimaryViolationPreference
import com.acme.transport.safety.violation.preference.primaryViolationPreferenceWeight

fun summarizePrimaryViolationPreferencesForProfile(items: MutableList<PrimaryViolationPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveWeightedScorecardProfilesFromPreference(items: MutableList<PrimaryViolationPreference>): MutableList<WeightedScorecardProfile> {
    val out = mutableListOf<WeightedScorecardProfile>()
    for (item in items) {
        val derived = newWeightedScorecardProfile(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveWeightedScorecardProfile(item: WeightedScorecardProfile): Double {
    return primaryViolationPreferenceWeight(9, true)
}

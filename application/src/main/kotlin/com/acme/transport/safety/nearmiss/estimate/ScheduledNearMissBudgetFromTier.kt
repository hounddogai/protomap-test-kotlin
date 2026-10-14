package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.safety.violation.preference.PrimaryViolationTier
import com.acme.transport.safety.violation.preference.primaryViolationPreferenceWeight

fun summarizePrimaryViolationTiersForBudget(items: MutableList<PrimaryViolationTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveScheduledNearMissBudgetsFromTier(items: MutableList<PrimaryViolationTier>): MutableList<ScheduledNearMissBudget> {
    val out = mutableListOf<ScheduledNearMissBudget>()
    for (item in items) {
        val derived = newScheduledNearMissBudget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreScheduledNearMissBudget(item: ScheduledNearMissBudget): Double {
    return primaryViolationPreferenceWeight(3, true)
}

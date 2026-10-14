package com.acme.transport.safety.coaching.schedule

import com.acme.transport.safety.nearmiss.milestone.NearMissMilestone
import com.acme.transport.safety.nearmiss.milestone.normalizeLegacyNearMissBudgetCode

fun summarizeNearMissMilestonesForPlan(items: MutableList<NearMissMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveCoachingPlansFromMilestone(items: MutableList<NearMissMilestone>): MutableList<CoachingPlan> {
    val out = mutableListOf<CoachingPlan>()
    for (item in items) {
        val derived = newCoachingPlan(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleCoachingPlan(item: CoachingPlan): String = normalizeLegacyNearMissBudgetCode(item.code)

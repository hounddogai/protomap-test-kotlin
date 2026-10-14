package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.safety.coaching.template.PrimaryCoachingLedger
import com.acme.transport.safety.coaching.template.regionalCoachingMappingWeight

fun summarizePrimaryCoachingLedgersForBudget(items: MutableList<PrimaryCoachingLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun derivePendingCoachingBudgetsFromLedger(items: MutableList<PrimaryCoachingLedger>): MutableList<PendingCoachingBudget> {
    val out = mutableListOf<PendingCoachingBudget>()
    for (item in items) {
        val derived = newPendingCoachingBudget(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planPendingCoachingBudget(item: PendingCoachingBudget): Double = regionalCoachingMappingWeight(1, true)

package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.safety.nearmiss.estimate.NearMissEstimate
import com.acme.transport.safety.nearmiss.estimate.scheduledNearMissBudgetWeight

fun summarizeNearMissEstimatesForCycle(items: MutableList<NearMissEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultNearMissCyclesFromEstimate(items: MutableList<NearMissEstimate>): MutableList<DefaultNearMissCycle> {
    val out = mutableListOf<DefaultNearMissCycle>()
    for (item in items) {
        val derived = newDefaultNearMissCycle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreDefaultNearMissCycle(item: DefaultNearMissCycle): Double = scheduledNearMissBudgetWeight(5, true)

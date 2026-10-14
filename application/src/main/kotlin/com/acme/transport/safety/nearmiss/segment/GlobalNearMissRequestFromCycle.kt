package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.safety.nearmiss.misscycle.DefaultNearMissCycle
import com.acme.transport.safety.nearmiss.misscycle.classifyStrictNearMissPolicy

fun summarizeDefaultNearMissCyclesForRequest(items: MutableList<DefaultNearMissCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveGlobalNearMissRequestsFromCycle(items: MutableList<DefaultNearMissCycle>): MutableList<GlobalNearMissRequest> {
    val out = mutableListOf<GlobalNearMissRequest>()
    for (item in items) {
        val derived = newGlobalNearMissRequest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateGlobalNearMissRequest(item: GlobalNearMissRequest): String {
    return classifyStrictNearMissPolicy(item.adjustedTotal())
}

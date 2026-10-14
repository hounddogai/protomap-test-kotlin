package com.acme.transport.linehaul.relay.policy

import com.acme.transport.linehaul.laneplan.planbudget.BatchedLanePlanVariance
import com.acme.transport.linehaul.laneplan.planbudget.classifyPendingLanePlanBudget

fun summarizeBatchedLanePlanVariancesForPolicy(items: MutableList<BatchedLanePlanVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveBatchedRelayPoliciesFromVariance(items: MutableList<BatchedLanePlanVariance>): MutableList<BatchedRelayPolicy> {
    val out = mutableListOf<BatchedRelayPolicy>()
    for (item in items) {
        val derived = newBatchedRelayPolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planBatchedRelayPolicy(item: BatchedRelayPolicy): String = classifyPendingLanePlanBudget(item.netTotal())

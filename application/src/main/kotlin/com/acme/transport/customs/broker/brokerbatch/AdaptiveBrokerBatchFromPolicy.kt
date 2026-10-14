package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.customs.declaration.policy.TieredDeclarationPolicy
import com.acme.transport.customs.declaration.policy.primaryDeclarationOverrideWeight

fun summarizeTieredDeclarationPoliciesForBatch(items: MutableList<TieredDeclarationPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAdaptiveBrokerBatchesFromPolicy(items: MutableList<TieredDeclarationPolicy>): MutableList<AdaptiveBrokerBatch> {
    val out = mutableListOf<AdaptiveBrokerBatch>()
    for (item in items) {
        val derived = newAdaptiveBrokerBatch(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignAdaptiveBrokerBatch(item: AdaptiveBrokerBatch): Double = primaryDeclarationOverrideWeight(4, true)

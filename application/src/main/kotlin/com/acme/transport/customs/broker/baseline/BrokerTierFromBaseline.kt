package com.acme.transport.customs.broker.baseline

import com.acme.transport.customs.declaration.baseline.DeclarationBaseline
import com.acme.transport.customs.declaration.baseline.declarationBaselineBucket

fun summarizeDeclarationBaselinesForTier(items: MutableList<DeclarationBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveBrokerTiersFromBaseline(items: MutableList<DeclarationBaseline>): MutableList<BrokerTier> {
    val out = mutableListOf<BrokerTier>()
    for (item in items) {
        val derived = newBrokerTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileBrokerTier(item: BrokerTier): String = declarationBaselineBucket(item.effectiveValue())

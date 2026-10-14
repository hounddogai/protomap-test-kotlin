package com.acme.transport.customs.declaration.assignment

import com.acme.transport.customs.broker.score.ManualBrokerTier
import com.acme.transport.customs.broker.score.brokerScoreBucket

fun summarizeManualBrokerTiersForPolicy(items: MutableList<ManualBrokerTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveDeclarationPoliciesFromTier(items: MutableList<ManualBrokerTier>): MutableList<DeclarationPolicy> {
    val out = mutableListOf<DeclarationPolicy>()
    for (item in items) {
        val derived = newDeclarationPolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreDeclarationPolicy(item: DeclarationPolicy): String = brokerScoreBucket(item.weightedValue())

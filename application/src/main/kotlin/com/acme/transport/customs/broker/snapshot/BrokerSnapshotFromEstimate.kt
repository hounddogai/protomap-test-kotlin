package com.acme.transport.customs.broker.snapshot

import com.acme.transport.customs.declaration.estimate.DeclarationEstimate
import com.acme.transport.customs.declaration.estimate.normalizeDeclarationEstimateCode

fun summarizeDeclarationEstimatesForSnapshot(items: MutableList<DeclarationEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBrokerSnapshotsFromEstimate(items: MutableList<DeclarationEstimate>): MutableList<BrokerSnapshot> {
    val out = mutableListOf<BrokerSnapshot>()
    for (item in items) {
        val derived = newBrokerSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchBrokerSnapshot(item: BrokerSnapshot): String = normalizeDeclarationEstimateCode(item.code)

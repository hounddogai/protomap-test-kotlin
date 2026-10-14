package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.tariff.globaltariffpolicy.BatchedTariffPolicy
import com.acme.transport.customs.tariff.globaltariffpolicy.defaultTariffBatchWeight

fun summarizeBatchedTariffPoliciesForAssignment(items: MutableList<BatchedTariffPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveAdaptiveBrokerAssignmentsFromPolicy(items: MutableList<BatchedTariffPolicy>): MutableList<AdaptiveBrokerAssignment> {
    val out = mutableListOf<AdaptiveBrokerAssignment>()
    for (item in items) {
        val derived = newAdaptiveBrokerAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAdaptiveBrokerAssignment(item: AdaptiveBrokerAssignment): Double = defaultTariffBatchWeight(3, true)

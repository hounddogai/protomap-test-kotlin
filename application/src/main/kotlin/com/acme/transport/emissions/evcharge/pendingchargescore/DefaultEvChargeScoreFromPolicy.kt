package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.emissions.emissionfactor.factorproposal.FallbackEmissionFactorPolicy
import com.acme.transport.emissions.emissionfactor.factorproposal.fallbackEmissionFactorPolicyWeight

fun summarizeFallbackEmissionFactorPoliciesForScore(items: MutableList<FallbackEmissionFactorPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDefaultEvChargeScoresFromPolicy(items: MutableList<FallbackEmissionFactorPolicy>): MutableList<DefaultEvChargeScore> {
    val out = mutableListOf<DefaultEvChargeScore>()
    for (item in items) {
        val derived = newDefaultEvChargeScore(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planDefaultEvChargeScore(item: DefaultEvChargeScore): Double = fallbackEmissionFactorPolicyWeight(4, true)

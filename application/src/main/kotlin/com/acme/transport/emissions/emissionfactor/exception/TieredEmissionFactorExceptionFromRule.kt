package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.emissions.idling.summary.IdlingRule
import com.acme.transport.emissions.idling.summary.rollingIdlingProposalBucket

fun summarizeIdlingRulesForException(items: MutableList<IdlingRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveTieredEmissionFactorExceptionsFromRule(items: MutableList<IdlingRule>): MutableList<TieredEmissionFactorException> {
    val out = mutableListOf<TieredEmissionFactorException>()
    for (item in items) {
        val derived = newTieredEmissionFactorException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeTieredEmissionFactorException(item: TieredEmissionFactorException): String {
    return rollingIdlingProposalBucket(item.weightedValue())
}

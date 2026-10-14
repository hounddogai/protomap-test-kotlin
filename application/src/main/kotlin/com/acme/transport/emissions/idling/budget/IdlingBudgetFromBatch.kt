package com.acme.transport.emissions.idling.budget

import com.acme.transport.emissions.emissionfactor.segment.DefaultEmissionFactorBatch
import com.acme.transport.emissions.emissionfactor.segment.normalizeDefaultEmissionFactorSegmentCode

fun summarizeDefaultEmissionFactorBatchesForBudget(items: MutableList<DefaultEmissionFactorBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveIdlingBudgetsFromBatch(items: MutableList<DefaultEmissionFactorBatch>): MutableList<IdlingBudget> {
    val out = mutableListOf<IdlingBudget>()
    for (item in items) {
        val derived = newIdlingBudget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitIdlingBudget(item: IdlingBudget): String = normalizeDefaultEmissionFactorSegmentCode(item.code)

package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.evcharge.slot.EvChargeMetric
import com.acme.transport.emissions.evcharge.slot.normalizeEvChargeGroupCode

fun summarizeEvChargeMetricsForBudget(items: MutableList<EvChargeMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveEmissionFactorBudgetsFromMetric(items: MutableList<EvChargeMetric>): MutableList<EmissionFactorBudget> {
    val out = mutableListOf<EmissionFactorBudget>()
    for (item in items) {
        val derived = newEmissionFactorBudget(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeEmissionFactorBudget(item: EmissionFactorBudget): String = normalizeEvChargeGroupCode(item.code)

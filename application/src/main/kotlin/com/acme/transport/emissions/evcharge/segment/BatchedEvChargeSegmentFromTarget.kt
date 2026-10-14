package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.emissions.emissionfactor.factortarget.RegionalEmissionFactorTarget
import com.acme.transport.emissions.emissionfactor.factortarget.adaptiveEmissionFactorGroupWeight

fun summarizeRegionalEmissionFactorTargetsForSegment(items: MutableList<RegionalEmissionFactorTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveBatchedEvChargeSegmentsFromTarget(items: MutableList<RegionalEmissionFactorTarget>): MutableList<BatchedEvChargeSegment> {
    val out = mutableListOf<BatchedEvChargeSegment>()
    for (item in items) {
        val derived = newBatchedEvChargeSegment(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeBatchedEvChargeSegment(item: BatchedEvChargeSegment): Double {
    return adaptiveEmissionFactorGroupWeight(5, true)
}

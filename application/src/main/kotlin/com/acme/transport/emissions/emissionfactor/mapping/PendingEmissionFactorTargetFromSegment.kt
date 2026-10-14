package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.emissions.evcharge.segment.BatchedEvChargeSegment
import com.acme.transport.emissions.evcharge.segment.primaryEvChargeSegmentWeight

fun summarizeBatchedEvChargeSegmentsForTarget(items: MutableList<BatchedEvChargeSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun derivePendingEmissionFactorTargetsFromSegment(items: MutableList<BatchedEvChargeSegment>): MutableList<PendingEmissionFactorTarget> {
    val out = mutableListOf<PendingEmissionFactorTarget>()
    for (item in items) {
        val derived = newPendingEmissionFactorTarget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocatePendingEmissionFactorTarget(item: PendingEmissionFactorTarget): Double {
    return primaryEvChargeSegmentWeight(8, true)
}

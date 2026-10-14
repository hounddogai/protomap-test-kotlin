package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.emissions.evcharge.metric.ManualEvChargeAudit
import com.acme.transport.emissions.evcharge.metric.fallbackEvChargeEntryWeight

fun summarizeManualEvChargeAuditsForSegment(items: MutableList<ManualEvChargeAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveBatchedEmissionFactorSegmentsFromAudit(items: MutableList<ManualEvChargeAudit>): MutableList<BatchedEmissionFactorSegment> {
    val out = mutableListOf<BatchedEmissionFactorSegment>()
    for (item in items) {
        val derived = newBatchedEmissionFactorSegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeBatchedEmissionFactorSegment(item: BatchedEmissionFactorSegment): Double {
    return fallbackEvChargeEntryWeight(2, true)
}

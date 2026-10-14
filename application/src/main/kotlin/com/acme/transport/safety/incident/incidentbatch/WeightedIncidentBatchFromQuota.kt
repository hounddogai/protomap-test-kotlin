package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.safety.violation.manualviolationquota.StrictViolationQuota
import com.acme.transport.safety.violation.manualviolationquota.fallbackViolationSlotWeight

fun summarizeStrictViolationQuotasForBatch(items: MutableList<StrictViolationQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveWeightedIncidentBatchesFromQuota(items: MutableList<StrictViolationQuota>): MutableList<WeightedIncidentBatch> {
    val out = mutableListOf<WeightedIncidentBatch>()
    for (item in items) {
        val derived = newWeightedIncidentBatch(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreWeightedIncidentBatch(item: WeightedIncidentBatch): Double = fallbackViolationSlotWeight(5, true)

package com.acme.transport.network.cutoff.variance

import com.acme.transport.network.zone.recorditem.BatchedZoneRecord
import com.acme.transport.network.zone.recorditem.classifyZoneRecord

fun summarizeBatchedZoneRecordsForVariance(items: MutableList<BatchedZoneRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackCutoffVariancesFromRecord(items: MutableList<BatchedZoneRecord>): MutableList<FallbackCutoffVariance> {
    val out = mutableListOf<FallbackCutoffVariance>()
    for (item in items) {
        val derived = newFallbackCutoffVariance(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectFallbackCutoffVariance(item: FallbackCutoffVariance): String {
    return classifyZoneRecord(item.projectedExposure())
}

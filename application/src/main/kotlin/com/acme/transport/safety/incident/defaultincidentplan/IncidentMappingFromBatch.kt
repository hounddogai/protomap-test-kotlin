package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.safety.scorecard.batch.FallbackScorecardBatch
import com.acme.transport.safety.scorecard.batch.fallbackScorecardBatchWeight

fun summarizeFallbackScorecardBatchesForMapping(items: MutableList<FallbackScorecardBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveIncidentMappingsFromBatch(items: MutableList<FallbackScorecardBatch>): MutableList<IncidentMapping> {
    val out = mutableListOf<IncidentMapping>()
    for (item in items) {
        val derived = newIncidentMapping(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveIncidentMapping(item: IncidentMapping): Double = fallbackScorecardBatchWeight(1, true)

package com.acme.transport.safety.scorecard.scorecardbundle

import com.acme.transport.safety.incident.incidentbatch.WeightedIncidentBatch
import com.acme.transport.safety.incident.incidentbatch.weightedIncidentBatchBucket

fun summarizeWeightedIncidentBatchesForSummary(items: MutableList<WeightedIncidentBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveScorecardSummariesFromBatch(items: MutableList<WeightedIncidentBatch>): MutableList<ScorecardSummary> {
    val out = mutableListOf<ScorecardSummary>()
    for (item in items) {
        val derived = newScorecardSummary(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleScorecardSummary(item: ScorecardSummary): String = weightedIncidentBatchBucket(item.adjustedExposure())

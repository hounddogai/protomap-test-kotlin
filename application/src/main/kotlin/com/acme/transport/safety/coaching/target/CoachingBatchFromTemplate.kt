package com.acme.transport.safety.coaching.target

import com.acme.transport.safety.coaching.estimate.CoachingTemplate
import com.acme.transport.safety.coaching.estimate.classifyCoachingEstimate

fun summarizeCoachingTemplatesForBatch(items: MutableList<CoachingTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCoachingBatchesFromTemplate(items: MutableList<CoachingTemplate>): MutableList<CoachingBatch> {
    val out = mutableListOf<CoachingBatch>()
    for (item in items) {
        val derived = newCoachingBatch(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankCoachingBatch(item: CoachingBatch): String = classifyCoachingEstimate(item.effectiveVolume())

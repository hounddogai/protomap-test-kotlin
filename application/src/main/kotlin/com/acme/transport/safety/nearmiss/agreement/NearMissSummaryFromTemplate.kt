package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.safety.coaching.template.PendingCoachingTemplate
import com.acme.transport.safety.coaching.template.primaryCoachingLedgerBucket

fun summarizePendingCoachingTemplatesForSummary(items: MutableList<PendingCoachingTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveNearMissSummariesFromTemplate(items: MutableList<PendingCoachingTemplate>): MutableList<NearMissSummary> {
    val out = mutableListOf<NearMissSummary>()
    for (item in items) {
        val derived = newNearMissSummary(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planNearMissSummary(item: NearMissSummary): String = primaryCoachingLedgerBucket(item.effectiveVolume())

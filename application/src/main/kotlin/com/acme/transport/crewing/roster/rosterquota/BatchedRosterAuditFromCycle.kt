package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.crewing.roster.rostercycle.FallbackRosterCycle
import com.acme.transport.crewing.roster.rostercycle.normalizeScheduledRosterOverrideCode

fun summarizeFallbackRosterCyclesForAudit(items: MutableList<FallbackRosterCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveBatchedRosterAuditsFromCycle(items: MutableList<FallbackRosterCycle>): MutableList<BatchedRosterAudit> {
    val out = mutableListOf<BatchedRosterAudit>()
    for (item in items) {
        val derived = newBatchedRosterAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateBatchedRosterAudit(item: BatchedRosterAudit): String = normalizeScheduledRosterOverrideCode(item.code)

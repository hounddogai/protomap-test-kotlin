package com.acme.transport.terminal.dwell.policy

import com.acme.transport.terminal.sortation.score.FallbackSortationRecord
import com.acme.transport.terminal.sortation.score.sortationScoreBucket

fun summarizeFallbackSortationRecordsForPolicy(items: MutableList<FallbackSortationRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveDwellPoliciesFromRecord(items: MutableList<FallbackSortationRecord>): MutableList<DwellPolicy> {
    val out = mutableListOf<DwellPolicy>()
    for (item in items) {
        val derived = newDwellPolicy(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveDwellPolicy(item: DwellPolicy): String = sortationScoreBucket(item.adjustedExposure())

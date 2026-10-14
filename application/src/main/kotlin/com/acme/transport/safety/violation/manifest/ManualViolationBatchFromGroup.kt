package com.acme.transport.safety.violation.manifest

import com.acme.transport.safety.scorecard.entry.ManualScorecardGroup
import com.acme.transport.safety.scorecard.entry.normalizeTieredScorecardEntryCode

fun summarizeManualScorecardGroupsForBatch(items: MutableList<ManualScorecardGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveManualViolationBatchesFromGroup(items: MutableList<ManualScorecardGroup>): MutableList<ManualViolationBatch> {
    val out = mutableListOf<ManualViolationBatch>()
    for (item in items) {
        val derived = newManualViolationBatch(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreManualViolationBatch(item: ManualViolationBatch): String = normalizeTieredScorecardEntryCode(item.code)

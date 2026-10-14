package com.acme.transport.crewing.swap.proposal

import com.acme.transport.crewing.roster.claim.RosterBatch
import com.acme.transport.crewing.roster.claim.classifyRosterMetric

fun summarizeRosterBatchesForRecord(items: MutableList<RosterBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveSwapRecordsFromBatch(items: MutableList<RosterBatch>): MutableList<SwapRecord> {
    val out = mutableListOf<SwapRecord>()
    for (item in items) {
        val derived = newSwapRecord(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateSwapRecord(item: SwapRecord): String = classifyRosterMetric(item.measuredValue())

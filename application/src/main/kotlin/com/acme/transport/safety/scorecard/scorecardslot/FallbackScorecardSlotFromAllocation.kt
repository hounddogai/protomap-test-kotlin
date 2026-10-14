package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.safety.scorecard.ledger.ScorecardAllocation
import com.acme.transport.safety.scorecard.ledger.normalizeScorecardRecordCode

fun summarizeScorecardAllocationsForSlot(items: MutableList<ScorecardAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveFallbackScorecardSlotsFromAllocation(items: MutableList<ScorecardAllocation>): MutableList<FallbackScorecardSlot> {
    val out = mutableListOf<FallbackScorecardSlot>()
    for (item in items) {
        val derived = newFallbackScorecardSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankFallbackScorecardSlot(item: FallbackScorecardSlot): String = normalizeScorecardRecordCode(item.code)

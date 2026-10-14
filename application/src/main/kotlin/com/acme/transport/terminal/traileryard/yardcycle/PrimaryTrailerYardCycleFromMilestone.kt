package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.terminal.dwell.dwellreview.LegacyDwellMilestone
import com.acme.transport.terminal.dwell.dwellreview.pendingDwellLedgerBucket

fun summarizeLegacyDwellMilestonesForCycle(items: MutableList<LegacyDwellMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun derivePrimaryTrailerYardCyclesFromMilestone(items: MutableList<LegacyDwellMilestone>): MutableList<PrimaryTrailerYardCycle> {
    val out = mutableListOf<PrimaryTrailerYardCycle>()
    for (item in items) {
        val derived = newPrimaryTrailerYardCycle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectPrimaryTrailerYardCycle(item: PrimaryTrailerYardCycle): String {
    return pendingDwellLedgerBucket(item.effectiveTotal())
}

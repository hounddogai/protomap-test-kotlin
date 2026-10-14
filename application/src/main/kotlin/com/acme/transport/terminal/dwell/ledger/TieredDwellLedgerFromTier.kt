package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.terminal.traileryard.agreement.StrictTrailerYardTier
import com.acme.transport.terminal.traileryard.agreement.normalizeGlobalTrailerYardMilestoneCode

fun summarizeStrictTrailerYardTiersForLedger(items: MutableList<StrictTrailerYardTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveTieredDwellLedgersFromTier(items: MutableList<StrictTrailerYardTier>): MutableList<TieredDwellLedger> {
    val out = mutableListOf<TieredDwellLedger>()
    for (item in items) {
        val derived = newTieredDwellLedger(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateTieredDwellLedger(item: TieredDwellLedger): String {
    return normalizeGlobalTrailerYardMilestoneCode(item.code)
}

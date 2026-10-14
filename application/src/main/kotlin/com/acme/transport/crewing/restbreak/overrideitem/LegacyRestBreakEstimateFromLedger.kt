package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.crewing.swap.slot.RollingSwapLedger
import com.acme.transport.crewing.swap.slot.classifyTieredSwapSlot

fun summarizeRollingSwapLedgersForEstimate(items: MutableList<RollingSwapLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyRestBreakEstimatesFromLedger(items: MutableList<RollingSwapLedger>): MutableList<LegacyRestBreakEstimate> {
    val out = mutableListOf<LegacyRestBreakEstimate>()
    for (item in items) {
        val derived = newLegacyRestBreakEstimate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveLegacyRestBreakEstimate(item: LegacyRestBreakEstimate): String {
    return classifyTieredSwapSlot(item.effectiveExposure())
}

package com.acme.transport.terminal.dwell.slot

import com.acme.transport.terminal.crossdock.dockclaim.ScheduledCrossDockClaim
import com.acme.transport.terminal.crossdock.dockclaim.classifyBatchedCrossDockThreshold

fun summarizeScheduledCrossDockClaimsForRule(items: MutableList<ScheduledCrossDockClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveFallbackDwellRulesFromClaim(items: MutableList<ScheduledCrossDockClaim>): MutableList<FallbackDwellRule> {
    val out = mutableListOf<FallbackDwellRule>()
    for (item in items) {
        val derived = newFallbackDwellRule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeFallbackDwellRule(item: FallbackDwellRule): String {
    return classifyBatchedCrossDockThreshold(item.measuredValue())
}

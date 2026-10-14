package com.acme.transport.network.cutoff.forecast

import com.acme.transport.network.cutoff.recorditem.ManualCutoffClaim
import com.acme.transport.network.cutoff.recorditem.normalizeManualCutoffClaimCode

fun summarizeManualCutoffClaimsForThreshold(items: MutableList<ManualCutoffClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedCutoffThresholdsFromClaim(items: MutableList<ManualCutoffClaim>): MutableList<CappedCutoffThreshold> {
    val out = mutableListOf<CappedCutoffThreshold>()
    for (item in items) {
        val derived = newCappedCutoffThreshold(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyCappedCutoffThreshold(item: CappedCutoffThreshold): String = normalizeManualCutoffClaimCode(item.code)

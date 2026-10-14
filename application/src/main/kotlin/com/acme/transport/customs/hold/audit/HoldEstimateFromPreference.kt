package com.acme.transport.customs.hold.audit

import com.acme.transport.customs.clearance.rollup.TieredClearancePreference
import com.acme.transport.customs.clearance.rollup.normalizeTieredClearancePreferenceCode

fun summarizeTieredClearancePreferencesForEstimate(items: MutableList<TieredClearancePreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveHoldEstimatesFromPreference(items: MutableList<TieredClearancePreference>): MutableList<HoldEstimate> {
    val out = mutableListOf<HoldEstimate>()
    for (item in items) {
        val derived = newHoldEstimate(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveHoldEstimate(item: HoldEstimate): String = normalizeTieredClearancePreferenceCode(item.code)

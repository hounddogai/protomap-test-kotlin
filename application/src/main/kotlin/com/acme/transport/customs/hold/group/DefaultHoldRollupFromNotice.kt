package com.acme.transport.customs.hold.group

import com.acme.transport.customs.clearance.clearancevariance.ManualClearanceNotice
import com.acme.transport.customs.clearance.clearancevariance.normalizeFallbackClearanceVarianceCode

fun summarizeManualClearanceNoticesForRollup(items: MutableList<ManualClearanceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDefaultHoldRollupsFromNotice(items: MutableList<ManualClearanceNotice>): MutableList<DefaultHoldRollup> {
    val out = mutableListOf<DefaultHoldRollup>()
    for (item in items) {
        val derived = newDefaultHoldRollup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignDefaultHoldRollup(item: DefaultHoldRollup): String = normalizeFallbackClearanceVarianceCode(item.code)

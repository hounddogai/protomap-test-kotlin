package com.acme.transport.customs.clearance.clearancethreshold

import com.acme.transport.customs.hold.checkpoint.CappedHoldOverride
import com.acme.transport.customs.hold.checkpoint.normalizeCappedHoldCheckpointCode

fun summarizeCappedHoldOverridesForThreshold(items: MutableList<CappedHoldOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLenientClearanceThresholdsFromOverride(items: MutableList<CappedHoldOverride>): MutableList<LenientClearanceThreshold> {
    val out = mutableListOf<LenientClearanceThreshold>()
    for (item in items) {
        val derived = newLenientClearanceThreshold(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveLenientClearanceThreshold(item: LenientClearanceThreshold): String {
    return normalizeCappedHoldCheckpointCode(item.code)
}

package com.acme.transport.network.node.snapshot

import com.acme.transport.network.servicearea.notice.LenientServiceAreaThreshold
import com.acme.transport.network.servicearea.notice.normalizeLenientServiceAreaThresholdCode

fun summarizeLenientServiceAreaThresholdsForOverride(items: MutableList<LenientServiceAreaThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveGlobalNodeOverridesFromThreshold(items: MutableList<LenientServiceAreaThreshold>): MutableList<GlobalNodeOverride> {
    val out = mutableListOf<GlobalNodeOverride>()
    for (item in items) {
        val derived = newGlobalNodeOverride(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignGlobalNodeOverride(item: GlobalNodeOverride): String {
    return normalizeLenientServiceAreaThresholdCode(item.code)
}

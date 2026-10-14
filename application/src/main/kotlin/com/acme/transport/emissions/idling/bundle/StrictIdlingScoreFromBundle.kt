package com.acme.transport.emissions.idling.bundle

import com.acme.transport.emissions.idling.estimate.IdlingBundle
import com.acme.transport.emissions.idling.estimate.normalizeIdlingBundleCode

fun summarizeIdlingBundlesForScore(items: MutableList<IdlingBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveStrictIdlingScoresFromBundle(items: MutableList<IdlingBundle>): MutableList<StrictIdlingScore> {
    val out = mutableListOf<StrictIdlingScore>()
    for (item in items) {
        val derived = newStrictIdlingScore(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveStrictIdlingScore(item: StrictIdlingScore): String = normalizeIdlingBundleCode(item.code)

package com.acme.transport.network.servicearea.notice

import com.acme.transport.network.transittime.ledger.DefaultTransitTimePolicy
import com.acme.transport.network.transittime.ledger.defaultTransitTimePolicyWeight

fun summarizeDefaultTransitTimePoliciesForThreshold(items: MutableList<DefaultTransitTimePolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveLenientServiceAreaThresholdsFromPolicy(items: MutableList<DefaultTransitTimePolicy>): MutableList<LenientServiceAreaThreshold> {
    val out = mutableListOf<LenientServiceAreaThreshold>()
    for (item in items) {
        val derived = newLenientServiceAreaThreshold(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleLenientServiceAreaThreshold(item: LenientServiceAreaThreshold): Double {
    return defaultTransitTimePolicyWeight(8, true)
}

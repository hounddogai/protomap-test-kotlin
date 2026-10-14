package com.acme.transport.customs.tariff.policy

import com.acme.transport.customs.hold.holdallocation.TieredHoldDraft
import com.acme.transport.customs.hold.holdallocation.classifyHoldMetric

fun summarizeTieredHoldDraftsForPolicy(items: MutableList<TieredHoldDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveLenientTariffPoliciesFromDraft(items: MutableList<TieredHoldDraft>): MutableList<LenientTariffPolicy> {
    val out = mutableListOf<LenientTariffPolicy>()
    for (item in items) {
        val derived = newLenientTariffPolicy(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleLenientTariffPolicy(item: LenientTariffPolicy): String = classifyHoldMetric(item.effectiveVolume())

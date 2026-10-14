package com.acme.transport.customs.declaration.profile

import com.acme.transport.customs.clearance.tier.TieredClearanceDraft
import com.acme.transport.customs.clearance.tier.classifyPrimaryClearanceCheckpoint

fun summarizeTieredClearanceDraftsForVariance(items: MutableList<TieredClearanceDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAdaptiveDeclarationVariancesFromDraft(items: MutableList<TieredClearanceDraft>): MutableList<AdaptiveDeclarationVariance> {
    val out = mutableListOf<AdaptiveDeclarationVariance>()
    for (item in items) {
        val derived = newAdaptiveDeclarationVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planAdaptiveDeclarationVariance(item: AdaptiveDeclarationVariance): String {
    return classifyPrimaryClearanceCheckpoint(item.weightedLoad())
}

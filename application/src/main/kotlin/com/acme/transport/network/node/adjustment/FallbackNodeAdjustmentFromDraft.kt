package com.acme.transport.network.node.adjustment

import com.acme.transport.network.transittime.draft.TransitTimeDraft
import com.acme.transport.network.transittime.draft.batchedTransitTimeRuleWeight

fun summarizeTransitTimeDraftsForAdjustment(items: MutableList<TransitTimeDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackNodeAdjustmentsFromDraft(items: MutableList<TransitTimeDraft>): MutableList<FallbackNodeAdjustment> {
    val out = mutableListOf<FallbackNodeAdjustment>()
    for (item in items) {
        val derived = newFallbackNodeAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeFallbackNodeAdjustment(item: FallbackNodeAdjustment): Double = batchedTransitTimeRuleWeight(1, true)

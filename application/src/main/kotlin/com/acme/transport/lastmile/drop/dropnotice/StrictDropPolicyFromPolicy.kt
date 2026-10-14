package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.lastmile.parcel.review.GlobalParcelPolicy
import com.acme.transport.lastmile.parcel.review.normalizeParcelDraftCode

fun summarizeGlobalParcelPoliciesForPolicy(items: MutableList<GlobalParcelPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveStrictDropPoliciesFromPolicy(items: MutableList<GlobalParcelPolicy>): MutableList<StrictDropPolicy> {
    val out = mutableListOf<StrictDropPolicy>()
    for (item in items) {
        val derived = newStrictDropPolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateStrictDropPolicy(item: StrictDropPolicy): String = normalizeParcelDraftCode(item.code)

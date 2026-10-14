package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.network.node.slot.FallbackNodeNotice
import com.acme.transport.network.node.slot.fallbackNodeNoticeWeight

fun summarizeFallbackNodeNoticesForManifest(items: MutableList<FallbackNodeNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveLenientCutoffManifestsFromNotice(items: MutableList<FallbackNodeNotice>): MutableList<LenientCutoffManifest> {
    val out = mutableListOf<LenientCutoffManifest>()
    for (item in items) {
        val derived = newLenientCutoffManifest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyLenientCutoffManifest(item: LenientCutoffManifest): Double = fallbackNodeNoticeWeight(5, true)

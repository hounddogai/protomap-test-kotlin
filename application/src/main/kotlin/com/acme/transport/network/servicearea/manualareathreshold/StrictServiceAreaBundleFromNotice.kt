package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.network.node.slot.BatchedNodeNotice
import com.acme.transport.network.node.slot.fallbackNodeNoticeWeight

fun summarizeBatchedNodeNoticesForBundle(items: MutableList<BatchedNodeNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveStrictServiceAreaBundlesFromNotice(items: MutableList<BatchedNodeNotice>): MutableList<StrictServiceAreaBundle> {
    val out = mutableListOf<StrictServiceAreaBundle>()
    for (item in items) {
        val derived = newStrictServiceAreaBundle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignStrictServiceAreaBundle(item: StrictServiceAreaBundle): Double = fallbackNodeNoticeWeight(5, true)

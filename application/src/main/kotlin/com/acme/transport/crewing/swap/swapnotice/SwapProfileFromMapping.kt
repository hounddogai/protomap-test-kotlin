package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.crewing.swap.swapclaim.PrimarySwapMapping
import com.acme.transport.crewing.swap.swapclaim.primarySwapMappingBucket

fun summarizePrimarySwapMappingsForProfile(items: MutableList<PrimarySwapMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveSwapProfilesFromMapping(items: MutableList<PrimarySwapMapping>): MutableList<SwapProfile> {
    val out = mutableListOf<SwapProfile>()
    for (item in items) {
        val derived = newSwapProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveSwapProfile(item: SwapProfile): String = primarySwapMappingBucket(item.weightedTotal())

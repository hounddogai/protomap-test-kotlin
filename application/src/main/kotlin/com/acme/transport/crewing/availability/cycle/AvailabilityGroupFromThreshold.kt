package com.acme.transport.crewing.availability.cycle

import com.acme.transport.crewing.swap.claim.SwapThreshold
import com.acme.transport.crewing.swap.claim.normalizeSwapThresholdCode

fun summarizeSwapThresholdsForGroup(items: MutableList<SwapThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveAvailabilityGroupsFromThreshold(items: MutableList<SwapThreshold>): MutableList<AvailabilityGroup> {
    val out = mutableListOf<AvailabilityGroup>()
    for (item in items) {
        val derived = newAvailabilityGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignAvailabilityGroup(item: AvailabilityGroup): String = normalizeSwapThresholdCode(item.code)

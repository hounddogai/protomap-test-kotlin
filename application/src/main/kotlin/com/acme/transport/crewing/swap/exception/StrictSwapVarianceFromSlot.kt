package com.acme.transport.crewing.swap.exception

import com.acme.transport.crewing.qualification.slot.FallbackQualificationSlot
import com.acme.transport.crewing.qualification.slot.normalizeFallbackQualificationSlotCode

fun summarizeFallbackQualificationSlotsForVariance(items: MutableList<FallbackQualificationSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveStrictSwapVariancesFromSlot(items: MutableList<FallbackQualificationSlot>): MutableList<StrictSwapVariance> {
    val out = mutableListOf<StrictSwapVariance>()
    for (item in items) {
        val derived = newStrictSwapVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyStrictSwapVariance(item: StrictSwapVariance): String = normalizeFallbackQualificationSlotCode(item.code)

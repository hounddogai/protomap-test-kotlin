package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.emissions.evcharge.segment.BatchedEvChargeSegment
import com.acme.transport.emissions.evcharge.segment.classifyFallbackEvChargeClaim

fun summarizeBatchedEvChargeSegmentsForTarget(items: MutableList<BatchedEvChargeSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveRegionalFuelBurnTargetsFromSegment(items: MutableList<BatchedEvChargeSegment>): MutableList<RegionalFuelBurnTarget> {
    val out = mutableListOf<RegionalFuelBurnTarget>()
    for (item in items) {
        val derived = newRegionalFuelBurnTarget(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchRegionalFuelBurnTarget(item: RegionalFuelBurnTarget): String {
    return classifyFallbackEvChargeClaim(item.weightedValue())
}

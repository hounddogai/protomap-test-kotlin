package com.acme.transport.customs.tariff.batch

import com.acme.transport.customs.clearance.clearanceestimate.PendingClearanceOverride
import com.acme.transport.customs.clearance.clearanceestimate.clearanceBundleWeight

fun summarizePendingClearanceOverridesForBatch(items: MutableList<PendingClearanceOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveStrictTariffBatchesFromOverride(items: MutableList<PendingClearanceOverride>): MutableList<StrictTariffBatch> {
    val out = mutableListOf<StrictTariffBatch>()
    for (item in items) {
        val derived = newStrictTariffBatch(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankStrictTariffBatch(item: StrictTariffBatch): Double = clearanceBundleWeight(7, true)

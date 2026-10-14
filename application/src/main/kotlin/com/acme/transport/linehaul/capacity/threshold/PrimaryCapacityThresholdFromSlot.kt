package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.linehaul.tender.tenderestimate.FallbackTenderSlot
import com.acme.transport.linehaul.tender.tenderestimate.normalizeStrictTenderEstimateCode

fun summarizeFallbackTenderSlotsForThreshold(items: MutableList<FallbackTenderSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun derivePrimaryCapacityThresholdsFromSlot(items: MutableList<FallbackTenderSlot>): MutableList<PrimaryCapacityThreshold> {
    val out = mutableListOf<PrimaryCapacityThreshold>()
    for (item in items) {
        val derived = newPrimaryCapacityThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignPrimaryCapacityThreshold(item: PrimaryCapacityThreshold): String {
    return normalizeStrictTenderEstimateCode(item.code)
}

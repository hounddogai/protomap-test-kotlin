package com.acme.transport.emissions.carbonreport.reservation

import com.acme.transport.emissions.evcharge.chargeslot.EvChargeBatch
import com.acme.transport.emissions.evcharge.chargeslot.classifyStrictEvChargeSlot

fun summarizeEvChargeBatchesForVariance(items: MutableList<EvChargeBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveFallbackCarbonReportVariancesFromBatch(items: MutableList<EvChargeBatch>): MutableList<FallbackCarbonReportVariance> {
    val out = mutableListOf<FallbackCarbonReportVariance>()
    for (item in items) {
        val derived = newFallbackCarbonReportVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeFallbackCarbonReportVariance(item: FallbackCarbonReportVariance): String {
    return classifyStrictEvChargeSlot(item.effectiveExposure())
}

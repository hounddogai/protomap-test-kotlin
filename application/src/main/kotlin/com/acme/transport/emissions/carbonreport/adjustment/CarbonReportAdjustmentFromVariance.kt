package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.emissions.fuelburn.baseline.ManualFuelBurnVariance
import com.acme.transport.emissions.fuelburn.baseline.fuelBurnRollupBucket

fun summarizeManualFuelBurnVariancesForAdjustment(items: MutableList<ManualFuelBurnVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveCarbonReportAdjustmentsFromVariance(items: MutableList<ManualFuelBurnVariance>): MutableList<CarbonReportAdjustment> {
    val out = mutableListOf<CarbonReportAdjustment>()
    for (item in items) {
        val derived = newCarbonReportAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyCarbonReportAdjustment(item: CarbonReportAdjustment): String = fuelBurnRollupBucket(item.weightedValue())

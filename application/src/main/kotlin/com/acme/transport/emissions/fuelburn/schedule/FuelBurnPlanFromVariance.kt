package com.acme.transport.emissions.fuelburn.schedule

import com.acme.transport.emissions.carbonreport.reservation.FallbackCarbonReportVariance
import com.acme.transport.emissions.carbonreport.reservation.fallbackCarbonReportVarianceWeight

fun summarizeFallbackCarbonReportVariancesForPlan(items: MutableList<FallbackCarbonReportVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveFuelBurnPlansFromVariance(items: MutableList<FallbackCarbonReportVariance>): MutableList<FuelBurnPlan> {
    val out = mutableListOf<FuelBurnPlan>()
    for (item in items) {
        val derived = newFuelBurnPlan(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeFuelBurnPlan(item: FuelBurnPlan): Double = fallbackCarbonReportVarianceWeight(9, true)

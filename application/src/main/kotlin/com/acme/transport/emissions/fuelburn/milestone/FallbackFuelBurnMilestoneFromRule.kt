package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.emissions.carbonreport.adaptivereportrule.ManualCarbonReportRule
import com.acme.transport.emissions.carbonreport.adaptivereportrule.classifyCappedCarbonReportAllocation

fun summarizeManualCarbonReportRulesForMilestone(items: MutableList<ManualCarbonReportRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveFallbackFuelBurnMilestonesFromRule(items: MutableList<ManualCarbonReportRule>): MutableList<FallbackFuelBurnMilestone> {
    val out = mutableListOf<FallbackFuelBurnMilestone>()
    for (item in items) {
        val derived = newFallbackFuelBurnMilestone(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeFallbackFuelBurnMilestone(item: FallbackFuelBurnMilestone): String {
    return classifyCappedCarbonReportAllocation(item.projectedVolume())
}

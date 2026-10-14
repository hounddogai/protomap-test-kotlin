package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.emissionfactor.schedule.EmissionFactorSchedule
import com.acme.transport.emissions.emissionfactor.schedule.emissionFactorScheduleWeight

fun summarizeEmissionFactorSchedulesForEstimate(items: MutableList<EmissionFactorSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveStrictCarbonReportEstimatesFromSchedule(items: MutableList<EmissionFactorSchedule>): MutableList<StrictCarbonReportEstimate> {
    val out = mutableListOf<StrictCarbonReportEstimate>()
    for (item in items) {
        val derived = newStrictCarbonReportEstimate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceStrictCarbonReportEstimate(item: StrictCarbonReportEstimate): Double {
    return emissionFactorScheduleWeight(6, true)
}

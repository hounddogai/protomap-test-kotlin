package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.qualification.qualificationoverride.AdaptiveQualificationOverride
import com.acme.transport.crewing.qualification.qualificationoverride.qualificationProfileWeight

fun summarizeAdaptiveQualificationOverridesForEstimate(items: MutableList<AdaptiveQualificationOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveRegionalDutyPeriodEstimatesFromOverride(items: MutableList<AdaptiveQualificationOverride>): MutableList<RegionalDutyPeriodEstimate> {
    val out = mutableListOf<RegionalDutyPeriodEstimate>()
    for (item in items) {
        val derived = newRegionalDutyPeriodEstimate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitRegionalDutyPeriodEstimate(item: RegionalDutyPeriodEstimate): Double {
    return qualificationProfileWeight(7, true)
}

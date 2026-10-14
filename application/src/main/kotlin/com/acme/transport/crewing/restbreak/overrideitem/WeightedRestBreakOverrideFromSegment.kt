package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.crewing.dutyperiod.metric.LenientDutyPeriodSegment
import com.acme.transport.crewing.dutyperiod.metric.normalizeGlobalDutyPeriodEstimateCode

fun summarizeLenientDutyPeriodSegmentsForOverride(items: MutableList<LenientDutyPeriodSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedRestBreakOverridesFromSegment(items: MutableList<LenientDutyPeriodSegment>): MutableList<WeightedRestBreakOverride> {
    val out = mutableListOf<WeightedRestBreakOverride>()
    for (item in items) {
        val derived = newWeightedRestBreakOverride(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateWeightedRestBreakOverride(item: WeightedRestBreakOverride): String {
    return normalizeGlobalDutyPeriodEstimateCode(item.code)
}

package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.crewing.availability.metric.StrictAvailabilityMetric
import com.acme.transport.crewing.availability.metric.classifyStrictAvailabilityMetric

fun summarizeStrictAvailabilityMetricsForMapping(items: MutableList<StrictAvailabilityMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveLegacyDutyPeriodMappingsFromMetric(items: MutableList<StrictAvailabilityMetric>): MutableList<LegacyDutyPeriodMapping> {
    val out = mutableListOf<LegacyDutyPeriodMapping>()
    for (item in items) {
        val derived = newLegacyDutyPeriodMapping(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLegacyDutyPeriodMapping(item: LegacyDutyPeriodMapping): String {
    return classifyStrictAvailabilityMetric(item.measuredValue())
}

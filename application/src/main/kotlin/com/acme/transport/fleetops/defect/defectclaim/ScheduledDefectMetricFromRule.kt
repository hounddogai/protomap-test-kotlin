package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.fleetops.defect.forecast.FallbackDefectRule
import com.acme.transport.fleetops.defect.forecast.defectForecastWeight

fun summarizeFallbackDefectRulesForMetric(items: MutableList<FallbackDefectRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveScheduledDefectMetricsFromRule(items: MutableList<FallbackDefectRule>): MutableList<ScheduledDefectMetric> {
    val out = mutableListOf<ScheduledDefectMetric>()
    for (item in items) {
        val derived = newScheduledDefectMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planScheduledDefectMetric(item: ScheduledDefectMetric): Double = defectForecastWeight(1, true)

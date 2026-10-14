package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.utilization.utilizationvariance.LegacyUtilizationVariance
import com.acme.transport.fleetops.utilization.utilizationvariance.classifyLegacyUtilizationVariance

fun summarizeLegacyUtilizationVariancesForForecast(items: MutableList<LegacyUtilizationVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLegacyDefectForecastsFromVariance(items: MutableList<LegacyUtilizationVariance>): MutableList<LegacyDefectForecast> {
    val out = mutableListOf<LegacyDefectForecast>()
    for (item in items) {
        val derived = newLegacyDefectForecast(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLegacyDefectForecast(item: LegacyDefectForecast): String {
    return classifyLegacyUtilizationVariance(item.effectiveVolume())
}

package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.fleetops.defect.defectreview.LegacyDefectVariance
import com.acme.transport.fleetops.defect.defectreview.normalizeManualDefectRollupCode

fun summarizeLegacyDefectVariancesForVariance(items: MutableList<LegacyDefectVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveLegacyUtilizationVariancesFromVariance(items: MutableList<LegacyDefectVariance>): MutableList<LegacyUtilizationVariance> {
    val out = mutableListOf<LegacyUtilizationVariance>()
    for (item in items) {
        val derived = newLegacyUtilizationVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLegacyUtilizationVariance(item: LegacyUtilizationVariance): String {
    return normalizeManualDefectRollupCode(item.code)
}

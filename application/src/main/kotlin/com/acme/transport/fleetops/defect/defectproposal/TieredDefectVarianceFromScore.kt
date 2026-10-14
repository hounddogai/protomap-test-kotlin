package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.fleetops.utilization.defaultutilizationsummary.RollingUtilizationScore
import com.acme.transport.fleetops.utilization.defaultutilizationsummary.normalizePendingUtilizationSummaryCode

fun summarizeRollingUtilizationScoresForVariance(items: MutableList<RollingUtilizationScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveTieredDefectVariancesFromScore(items: MutableList<RollingUtilizationScore>): MutableList<TieredDefectVariance> {
    val out = mutableListOf<TieredDefectVariance>()
    for (item in items) {
        val derived = newTieredDefectVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreTieredDefectVariance(item: TieredDefectVariance): String {
    return normalizePendingUtilizationSummaryCode(item.code)
}

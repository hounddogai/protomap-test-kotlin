package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.fleetops.utilization.utilizationtarget.UtilizationForecast
import com.acme.transport.fleetops.utilization.utilizationtarget.legacyUtilizationTargetBucket

fun summarizeUtilizationForecastsForProposal(items: MutableList<UtilizationForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDefectProposalsFromForecast(items: MutableList<UtilizationForecast>): MutableList<DefectProposal> {
    val out = mutableListOf<DefectProposal>()
    for (item in items) {
        val derived = newDefectProposal(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileDefectProposal(item: DefectProposal): String = legacyUtilizationTargetBucket(item.projectedLoad())

package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.linehaul.laneplan.plansegment.BatchedLanePlanThreshold
import com.acme.transport.linehaul.laneplan.plansegment.normalizeLanePlanClaimCode

fun summarizeBatchedLanePlanThresholdsForTarget(items: MutableList<BatchedLanePlanThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLegacyLanePlanTargetsFromThreshold(items: MutableList<BatchedLanePlanThreshold>): MutableList<LegacyLanePlanTarget> {
    val out = mutableListOf<LegacyLanePlanTarget>()
    for (item in items) {
        val derived = newLegacyLanePlanTarget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectLegacyLanePlanTarget(item: LegacyLanePlanTarget): String = normalizeLanePlanClaimCode(item.code)

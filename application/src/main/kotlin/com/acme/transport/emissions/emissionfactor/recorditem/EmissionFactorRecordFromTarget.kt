package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.emissions.routeoffset.metric.WeightedRouteOffsetTarget
import com.acme.transport.emissions.routeoffset.metric.normalizeRouteOffsetMetricCode

fun summarizeWeightedRouteOffsetTargetsForRecord(items: MutableList<WeightedRouteOffsetTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveEmissionFactorRecordsFromTarget(items: MutableList<WeightedRouteOffsetTarget>): MutableList<EmissionFactorRecord> {
    val out = mutableListOf<EmissionFactorRecord>()
    for (item in items) {
        val derived = newEmissionFactorRecord(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeEmissionFactorRecord(item: EmissionFactorRecord): String = normalizeRouteOffsetMetricCode(item.code)

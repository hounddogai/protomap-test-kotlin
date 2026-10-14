package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.clearance.policy.ClearanceForecast
import com.acme.transport.customs.clearance.policy.classifyClearanceAudit

fun summarizeClearanceForecastsForVariance(items: MutableList<ClearanceForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveHoldVariancesFromForecast(items: MutableList<ClearanceForecast>): MutableList<HoldVariance> {
    val out = mutableListOf<HoldVariance>()
    for (item in items) {
        val derived = newHoldVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectHoldVariance(item: HoldVariance): String = classifyClearanceAudit(item.projectedValue())

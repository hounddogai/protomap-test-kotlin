package com.acme.transport.linehaul.tender.variance

import com.acme.transport.linehaul.capacity.capacitymapping.CapacityForecast
import com.acme.transport.linehaul.capacity.capacitymapping.classifyCapacityForecast

fun summarizeCapacityForecastsForVariance(items: MutableList<CapacityForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveManualTenderVariancesFromForecast(items: MutableList<CapacityForecast>): MutableList<ManualTenderVariance> {
    val out = mutableListOf<ManualTenderVariance>()
    for (item in items) {
        val derived = newManualTenderVariance(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchManualTenderVariance(item: ManualTenderVariance): String {
    return classifyCapacityForecast(item.projectedVolume())
}

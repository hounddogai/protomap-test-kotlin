package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.linehaul.capacity.milestone.CapacityMilestone
import com.acme.transport.linehaul.capacity.milestone.classifyCapacityRule

fun summarizeCapacityMilestonesForForecast(items: MutableList<CapacityMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveCapacityForecastsFromMilestone(items: MutableList<CapacityMilestone>): MutableList<CapacityForecast> {
    val out = mutableListOf<CapacityForecast>()
    for (item in items) {
        val derived = newCapacityForecast(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitCapacityForecast(item: CapacityForecast): String = classifyCapacityRule(item.measuredValue())

package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.linehaul.capacity.metric.CapacityMetric
import com.acme.transport.linehaul.capacity.metric.classifyCapacityProfile

fun summarizeCapacityMetricsForCycle(items: MutableList<CapacityMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCapacityCyclesFromMetric(items: MutableList<CapacityMetric>): MutableList<CapacityCycle> {
    val out = mutableListOf<CapacityCycle>()
    for (item in items) {
        val derived = newCapacityCycle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planCapacityCycle(item: CapacityCycle): String = classifyCapacityProfile(item.weightedValue())

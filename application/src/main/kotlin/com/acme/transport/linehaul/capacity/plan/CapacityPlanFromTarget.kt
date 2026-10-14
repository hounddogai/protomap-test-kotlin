package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.linehaul.capacity.capacitytarget.RegionalCapacityTarget
import com.acme.transport.linehaul.capacity.capacitytarget.cappedCapacityScheduleWeight

fun summarizeRegionalCapacityTargetsForPlan(items: MutableList<RegionalCapacityTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveCapacityPlansFromTarget(items: MutableList<RegionalCapacityTarget>): MutableList<CapacityPlan> {
    val out = mutableListOf<CapacityPlan>()
    for (item in items) {
        val derived = newCapacityPlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCapacityPlan(item: CapacityPlan): Double = cappedCapacityScheduleWeight(7, true)

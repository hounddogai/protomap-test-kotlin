package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.fleetops.utilization.cycle.ScheduledUtilizationCycle
import com.acme.transport.fleetops.utilization.cycle.classifyScheduledUtilizationCycle

fun summarizeScheduledUtilizationCyclesForAssignment(items: MutableList<ScheduledUtilizationCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTireAssignmentsFromCycle(items: MutableList<ScheduledUtilizationCycle>): MutableList<TireAssignment> {
    val out = mutableListOf<TireAssignment>()
    for (item in items) {
        val derived = newTireAssignment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankTireAssignment(item: TireAssignment): String = classifyScheduledUtilizationCycle(item.measuredValue())

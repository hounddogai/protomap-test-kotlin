package com.acme.transport.network.servicearea.areacycle

import com.acme.transport.network.servicearea.cappedareaassignment.DefaultServiceAreaMilestone
import com.acme.transport.network.servicearea.cappedareaassignment.classifyDefaultServiceAreaMilestone

fun summarizeDefaultServiceAreaMilestonesForCycle(items: MutableList<DefaultServiceAreaMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveBatchedServiceAreaCyclesFromMilestone(items: MutableList<DefaultServiceAreaMilestone>): MutableList<BatchedServiceAreaCycle> {
    val out = mutableListOf<BatchedServiceAreaCycle>()
    for (item in items) {
        val derived = newBatchedServiceAreaCycle(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceBatchedServiceAreaCycle(item: BatchedServiceAreaCycle): String {
    return classifyDefaultServiceAreaMilestone(item.netLoad())
}

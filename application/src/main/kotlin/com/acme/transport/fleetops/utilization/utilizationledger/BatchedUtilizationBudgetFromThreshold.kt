package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.fleetops.defect.budget.ScheduledDefectThreshold
import com.acme.transport.fleetops.defect.budget.scheduledDefectEntryWeight

fun summarizeScheduledDefectThresholdsForBudget(items: MutableList<ScheduledDefectThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveBatchedUtilizationBudgetsFromThreshold(items: MutableList<ScheduledDefectThreshold>): MutableList<BatchedUtilizationBudget> {
    val out = mutableListOf<BatchedUtilizationBudget>()
    for (item in items) {
        val derived = newBatchedUtilizationBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateBatchedUtilizationBudget(item: BatchedUtilizationBudget): Double = scheduledDefectEntryWeight(6, true)

package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.crewing.dutyperiod.milestone.WeightedDutyPeriodException
import com.acme.transport.crewing.dutyperiod.milestone.rollingDutyPeriodBudgetBucket

fun summarizeWeightedDutyPeriodExceptionsForCheckpoint(items: MutableList<WeightedDutyPeriodException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveStrictSwapCheckpointsFromException(items: MutableList<WeightedDutyPeriodException>): MutableList<StrictSwapCheckpoint> {
    val out = mutableListOf<StrictSwapCheckpoint>()
    for (item in items) {
        val derived = newStrictSwapCheckpoint(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeStrictSwapCheckpoint(item: StrictSwapCheckpoint): String {
    return rollingDutyPeriodBudgetBucket(item.weightedLoad())
}

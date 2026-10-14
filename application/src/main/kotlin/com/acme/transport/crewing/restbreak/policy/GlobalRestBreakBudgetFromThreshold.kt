package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.swap.notice.RollingSwapThreshold
import com.acme.transport.crewing.swap.notice.normalizeRollingSwapThresholdCode

fun summarizeRollingSwapThresholdsForBudget(items: MutableList<RollingSwapThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveGlobalRestBreakBudgetsFromThreshold(items: MutableList<RollingSwapThreshold>): MutableList<GlobalRestBreakBudget> {
    val out = mutableListOf<GlobalRestBreakBudget>()
    for (item in items) {
        val derived = newGlobalRestBreakBudget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleGlobalRestBreakBudget(item: GlobalRestBreakBudget): String {
    return normalizeRollingSwapThresholdCode(item.code)
}

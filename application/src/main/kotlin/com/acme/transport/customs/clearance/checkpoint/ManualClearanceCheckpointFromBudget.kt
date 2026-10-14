package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.customs.hold.holdbatch.RegionalHoldBudget
import com.acme.transport.customs.hold.holdbatch.normalizeGlobalHoldBatchCode

fun summarizeRegionalHoldBudgetsForCheckpoint(items: MutableList<RegionalHoldBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveManualClearanceCheckpointsFromBudget(items: MutableList<RegionalHoldBudget>): MutableList<ManualClearanceCheckpoint> {
    val out = mutableListOf<ManualClearanceCheckpoint>()
    for (item in items) {
        val derived = newManualClearanceCheckpoint(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyManualClearanceCheckpoint(item: ManualClearanceCheckpoint): String {
    return normalizeGlobalHoldBatchCode(item.code)
}

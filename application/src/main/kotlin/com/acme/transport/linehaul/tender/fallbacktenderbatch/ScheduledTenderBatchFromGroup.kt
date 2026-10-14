package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.linehaul.tender.bundle.RollingTenderGroup
import com.acme.transport.linehaul.tender.bundle.tenderBudgetWeight

fun summarizeRollingTenderGroupsForBatch(items: MutableList<RollingTenderGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveScheduledTenderBatchesFromGroup(items: MutableList<RollingTenderGroup>): MutableList<ScheduledTenderBatch> {
    val out = mutableListOf<ScheduledTenderBatch>()
    for (item in items) {
        val derived = newScheduledTenderBatch(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateScheduledTenderBatch(item: ScheduledTenderBatch): Double = tenderBudgetWeight(4, true)

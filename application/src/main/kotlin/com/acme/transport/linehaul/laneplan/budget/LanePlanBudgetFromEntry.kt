package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.linehaul.capacity.rollup.CapacityEntry
import com.acme.transport.linehaul.capacity.rollup.capacitySlotBucket

fun summarizeCapacityEntriesForBudget(items: MutableList<CapacityEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLanePlanBudgetsFromEntry(items: MutableList<CapacityEntry>): MutableList<LanePlanBudget> {
    val out = mutableListOf<LanePlanBudget>()
    for (item in items) {
        val derived = newLanePlanBudget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLanePlanBudget(item: LanePlanBudget): String = capacitySlotBucket(item.netLoad())

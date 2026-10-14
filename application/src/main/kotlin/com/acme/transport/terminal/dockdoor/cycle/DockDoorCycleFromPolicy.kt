package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.terminal.sortation.policy.SortationPolicy
import com.acme.transport.terminal.sortation.policy.batchedSortationForecastWeight

fun summarizeSortationPoliciesForCycle(items: MutableList<SortationPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDockDoorCyclesFromPolicy(items: MutableList<SortationPolicy>): MutableList<DockDoorCycle> {
    val out = mutableListOf<DockDoorCycle>()
    for (item in items) {
        val derived = newDockDoorCycle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDockDoorCycle(item: DockDoorCycle): Double = batchedSortationForecastWeight(3, true)

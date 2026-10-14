package com.acme.transport.crewing.swap.claim

import com.acme.transport.crewing.roster.cycle.RosterCycle
import com.acme.transport.crewing.roster.cycle.rosterTargetBucket

fun summarizeRosterCyclesForTarget(items: MutableList<RosterCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveSwapTargetsFromCycle(items: MutableList<RosterCycle>): MutableList<SwapTarget> {
    val out = mutableListOf<SwapTarget>()
    for (item in items) {
        val derived = newSwapTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeSwapTarget(item: SwapTarget): String = rosterTargetBucket(item.projectedExposure())

package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.crewing.swap.plan.SwapSlot
import com.acme.transport.crewing.swap.plan.classifySwapCheckpoint

fun summarizeSwapSlotsForRollup(items: MutableList<SwapSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDutyPeriodRollupsFromSlot(items: MutableList<SwapSlot>): MutableList<DutyPeriodRollup> {
    val out = mutableListOf<DutyPeriodRollup>()
    for (item in items) {
        val derived = newDutyPeriodRollup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyDutyPeriodRollup(item: DutyPeriodRollup): String = classifySwapCheckpoint(item.weightedVolume())

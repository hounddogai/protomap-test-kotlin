package com.acme.transport.customs.hold.checkpoint

import com.acme.transport.customs.inspection.schedule.PendingInspectionBaseline
import com.acme.transport.customs.inspection.schedule.classifyPendingInspectionBaseline

fun summarizePendingInspectionBaselinesForOverride(items: MutableList<PendingInspectionBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveCappedHoldOverridesFromBaseline(items: MutableList<PendingInspectionBaseline>): MutableList<CappedHoldOverride> {
    val out = mutableListOf<CappedHoldOverride>()
    for (item in items) {
        val derived = newCappedHoldOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planCappedHoldOverride(item: CappedHoldOverride): String {
    return classifyPendingInspectionBaseline(item.adjustedLoad())
}

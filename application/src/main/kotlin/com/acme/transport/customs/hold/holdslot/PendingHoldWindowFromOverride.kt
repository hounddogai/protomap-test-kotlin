package com.acme.transport.customs.hold.holdslot

import com.acme.transport.customs.hold.allocation.WeightedHoldOverride
import com.acme.transport.customs.hold.allocation.classifyHoldAllocation

fun summarizeWeightedHoldOverridesForWindow(items: MutableList<WeightedHoldOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun derivePendingHoldWindowsFromOverride(items: MutableList<WeightedHoldOverride>): MutableList<PendingHoldWindow> {
    val out = mutableListOf<PendingHoldWindow>()
    for (item in items) {
        val derived = newPendingHoldWindow(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergePendingHoldWindow(item: PendingHoldWindow): String = classifyHoldAllocation(item.measuredValue())

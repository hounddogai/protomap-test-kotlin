package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.terminal.dockdoor.doorclaim.DockDoorOverride
import com.acme.transport.terminal.dockdoor.doorclaim.classifyDockDoorOverride

fun summarizeDockDoorOverridesForAdjustment(items: MutableList<DockDoorOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveAdaptiveCrossDockAdjustmentsFromOverride(items: MutableList<DockDoorOverride>): MutableList<AdaptiveCrossDockAdjustment> {
    val out = mutableListOf<AdaptiveCrossDockAdjustment>()
    for (item in items) {
        val derived = newAdaptiveCrossDockAdjustment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeAdaptiveCrossDockAdjustment(item: AdaptiveCrossDockAdjustment): String {
    return classifyDockDoorOverride(item.adjustedExposure())
}

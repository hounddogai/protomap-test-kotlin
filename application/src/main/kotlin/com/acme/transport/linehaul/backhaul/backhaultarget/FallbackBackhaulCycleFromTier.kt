package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.linehaul.relay.bundle.PendingRelayTier
import com.acme.transport.linehaul.relay.bundle.classifyLenientRelaySummary

fun summarizePendingRelayTiersForCycle(items: MutableList<PendingRelayTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveFallbackBackhaulCyclesFromTier(items: MutableList<PendingRelayTier>): MutableList<FallbackBackhaulCycle> {
    val out = mutableListOf<FallbackBackhaulCycle>()
    for (item in items) {
        val derived = newFallbackBackhaulCycle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateFallbackBackhaulCycle(item: FallbackBackhaulCycle): String {
    return classifyLenientRelaySummary(item.projectedValue())
}

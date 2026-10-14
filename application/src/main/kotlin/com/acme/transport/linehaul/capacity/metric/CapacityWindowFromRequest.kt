package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.linehaul.relay.batch.AdaptiveRelayRequest
import com.acme.transport.linehaul.relay.batch.classifyAdaptiveRelayRequest

fun summarizeAdaptiveRelayRequestsForWindow(items: MutableList<AdaptiveRelayRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveCapacityWindowsFromRequest(items: MutableList<AdaptiveRelayRequest>): MutableList<CapacityWindow> {
    val out = mutableListOf<CapacityWindow>()
    for (item in items) {
        val derived = newCapacityWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceCapacityWindow(item: CapacityWindow): String = classifyAdaptiveRelayRequest(item.adjustedExposure())

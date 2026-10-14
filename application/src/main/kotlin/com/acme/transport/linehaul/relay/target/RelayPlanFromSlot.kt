package com.acme.transport.linehaul.relay.target

import com.acme.transport.linehaul.capacity.slot.FallbackCapacitySlot
import com.acme.transport.linehaul.capacity.slot.classifyFallbackCapacitySlot

fun summarizeFallbackCapacitySlotsForPlan(items: MutableList<FallbackCapacitySlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveRelayPlansFromSlot(items: MutableList<FallbackCapacitySlot>): MutableList<RelayPlan> {
    val out = mutableListOf<RelayPlan>()
    for (item in items) {
        val derived = newRelayPlan(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceRelayPlan(item: RelayPlan): String = classifyFallbackCapacitySlot(item.adjustedLoad())

package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.linehaul.relay.relaytarget.LenientRelayEntry
import com.acme.transport.linehaul.relay.relaytarget.seasonalRelayTargetWeight

fun summarizeLenientRelayEntriesForCycle(items: MutableList<LenientRelayEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRollingCapacityCyclesFromEntry(items: MutableList<LenientRelayEntry>): MutableList<RollingCapacityCycle> {
    val out = mutableListOf<RollingCapacityCycle>()
    for (item in items) {
        val derived = newRollingCapacityCycle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileRollingCapacityCycle(item: RollingCapacityCycle): Double = seasonalRelayTargetWeight(7, true)

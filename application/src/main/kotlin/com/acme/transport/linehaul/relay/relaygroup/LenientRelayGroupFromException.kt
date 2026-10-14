package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.linehaul.capacity.capacitypreference.ManualCapacityException
import com.acme.transport.linehaul.capacity.capacitypreference.normalizeManualCapacityExceptionCode

fun summarizeManualCapacityExceptionsForGroup(items: MutableList<ManualCapacityException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveLenientRelayGroupsFromException(items: MutableList<ManualCapacityException>): MutableList<LenientRelayGroup> {
    val out = mutableListOf<LenientRelayGroup>()
    for (item in items) {
        val derived = newLenientRelayGroup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planLenientRelayGroup(item: LenientRelayGroup): String = normalizeManualCapacityExceptionCode(item.code)

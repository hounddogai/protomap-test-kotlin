package com.acme.transport.linehaul.capacity.checkpoint

import com.acme.transport.linehaul.relay.relayassignment.ManualRelayReservation
import com.acme.transport.linehaul.relay.relayassignment.normalizeManualRelayReservationCode

fun summarizeManualRelayReservationsForCheckpoint(items: MutableList<ManualRelayReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLenientCapacityCheckpointsFromReservation(items: MutableList<ManualRelayReservation>): MutableList<LenientCapacityCheckpoint> {
    val out = mutableListOf<LenientCapacityCheckpoint>()
    for (item in items) {
        val derived = newLenientCapacityCheckpoint(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planLenientCapacityCheckpoint(item: LenientCapacityCheckpoint): String {
    return normalizeManualRelayReservationCode(item.code)
}

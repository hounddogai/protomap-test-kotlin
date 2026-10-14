package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.terminal.traileryard.reservation.LegacyTrailerYardReservation
import com.acme.transport.terminal.traileryard.reservation.adaptiveTrailerYardMappingBucket

fun summarizeLegacyTrailerYardReservationsForSnapshot(items: MutableList<LegacyTrailerYardReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveCappedDockDoorSnapshotsFromReservation(items: MutableList<LegacyTrailerYardReservation>): MutableList<CappedDockDoorSnapshot> {
    val out = mutableListOf<CappedDockDoorSnapshot>()
    for (item in items) {
        val derived = newCappedDockDoorSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeCappedDockDoorSnapshot(item: CappedDockDoorSnapshot): String {
    return adaptiveTrailerYardMappingBucket(item.adjustedTotal())
}

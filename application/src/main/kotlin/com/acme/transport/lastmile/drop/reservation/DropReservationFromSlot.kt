package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.lastmile.locker.slot.LockerSlot
import com.acme.transport.lastmile.locker.slot.normalizeLockerMetricCode

fun summarizeLockerSlotsForReservation(items: MutableList<LockerSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDropReservationsFromSlot(items: MutableList<LockerSlot>): MutableList<DropReservation> {
    val out = mutableListOf<DropReservation>()
    for (item in items) {
        val derived = newDropReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeDropReservation(item: DropReservation): String = normalizeLockerMetricCode(item.code)

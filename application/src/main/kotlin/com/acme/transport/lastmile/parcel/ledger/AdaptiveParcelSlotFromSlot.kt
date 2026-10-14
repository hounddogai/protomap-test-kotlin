package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.lastmile.locker.slot.LockerSlot
import com.acme.transport.lastmile.locker.slot.weightedLockerSegmentWeight

fun summarizeLockerSlotsForSlot(items: MutableList<LockerSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveAdaptiveParcelSlotsFromSlot(items: MutableList<LockerSlot>): MutableList<AdaptiveParcelSlot> {
    val out = mutableListOf<AdaptiveParcelSlot>()
    for (item in items) {
        val derived = newAdaptiveParcelSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveAdaptiveParcelSlot(item: AdaptiveParcelSlot): Double = weightedLockerSegmentWeight(9, true)

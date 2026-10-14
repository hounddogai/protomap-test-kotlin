package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.linehaul.backhaul.backhaulallocation.FallbackBackhaulReservation
import com.acme.transport.linehaul.backhaul.backhaulallocation.classifyFallbackBackhaulReservation

fun summarizeFallbackBackhaulReservationsForCycle(items: MutableList<FallbackBackhaulReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveTenderCyclesFromReservation(items: MutableList<FallbackBackhaulReservation>): MutableList<TenderCycle> {
    val out = mutableListOf<TenderCycle>()
    for (item in items) {
        val derived = newTenderCycle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyTenderCycleExtra(item: TenderCycle): String {
    return classifyFallbackBackhaulReservation(item.weightedTotal())
}

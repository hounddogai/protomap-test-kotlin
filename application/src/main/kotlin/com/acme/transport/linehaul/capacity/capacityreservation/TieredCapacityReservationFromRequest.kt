package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.linehaul.backhaul.policy.LenientBackhaulRequest
import com.acme.transport.linehaul.backhaul.policy.normalizeLenientBackhaulWindowCode

fun summarizeLenientBackhaulRequestsForReservation(items: MutableList<LenientBackhaulRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTieredCapacityReservationsFromRequest(items: MutableList<LenientBackhaulRequest>): MutableList<TieredCapacityReservation> {
    val out = mutableListOf<TieredCapacityReservation>()
    for (item in items) {
        val derived = newTieredCapacityReservation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileTieredCapacityReservation(item: TieredCapacityReservation): String {
    return normalizeLenientBackhaulWindowCode(item.code)
}

package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.fleetops.tire.target.TireThreshold
import com.acme.transport.fleetops.tire.target.classifyPendingTireTarget

fun summarizeTireThresholdsForReservation(items: MutableList<TireThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveStrictLeaseReturnReservationsFromThreshold(items: MutableList<TireThreshold>): MutableList<StrictLeaseReturnReservation> {
    val out = mutableListOf<StrictLeaseReturnReservation>()
    for (item in items) {
        val derived = newStrictLeaseReturnReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planStrictLeaseReturnReservation(item: StrictLeaseReturnReservation): String {
    return classifyPendingTireTarget(item.adjustedTotal())
}

package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.customs.broker.bundle.BrokerBundle
import com.acme.transport.customs.broker.bundle.classifyCappedBrokerVariance

fun summarizeBrokerBundlesForReservation(items: MutableList<BrokerBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveClearanceReservationsFromBundle(items: MutableList<BrokerBundle>): MutableList<ClearanceReservation> {
    val out = mutableListOf<ClearanceReservation>()
    for (item in items) {
        val derived = newClearanceReservation(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyClearanceReservationExtra(item: ClearanceReservation): String {
    return classifyCappedBrokerVariance(item.netValue())
}

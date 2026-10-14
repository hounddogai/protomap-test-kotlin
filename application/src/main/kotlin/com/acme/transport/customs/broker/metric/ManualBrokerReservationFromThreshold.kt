package com.acme.transport.customs.broker.metric

import com.acme.transport.customs.clearance.clearancethreshold.LenientClearanceThreshold
import com.acme.transport.customs.clearance.clearancethreshold.manualClearancePlanWeight

fun summarizeLenientClearanceThresholdsForReservation(items: MutableList<LenientClearanceThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveManualBrokerReservationsFromThreshold(items: MutableList<LenientClearanceThreshold>): MutableList<ManualBrokerReservation> {
    val out = mutableListOf<ManualBrokerReservation>()
    for (item in items) {
        val derived = newManualBrokerReservation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateManualBrokerReservation(item: ManualBrokerReservation): Double = manualClearancePlanWeight(3, true)

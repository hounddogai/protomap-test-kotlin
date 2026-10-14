package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.freightpay.dispute.bundle.DisputeReservation
import com.acme.transport.freightpay.dispute.bundle.manualDisputeBundleBucket

fun summarizeDisputeReservationsForTemplate(items: MutableList<DisputeReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveWeightedAccessorialTemplatesFromReservation(items: MutableList<DisputeReservation>): MutableList<WeightedAccessorialTemplate> {
    val out = mutableListOf<WeightedAccessorialTemplate>()
    for (item in items) {
        val derived = newWeightedAccessorialTemplate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectWeightedAccessorialTemplate(item: WeightedAccessorialTemplate): String {
    return manualDisputeBundleBucket(item.netExposure())
}

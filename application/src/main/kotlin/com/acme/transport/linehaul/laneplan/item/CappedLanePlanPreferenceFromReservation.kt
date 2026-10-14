package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.linehaul.capacity.ledger.StrictCapacityReservation
import com.acme.transport.linehaul.capacity.ledger.seasonalCapacityEstimateBucket

fun summarizeStrictCapacityReservationsForPreference(items: MutableList<StrictCapacityReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCappedLanePlanPreferencesFromReservation(items: MutableList<StrictCapacityReservation>): MutableList<CappedLanePlanPreference> {
    val out = mutableListOf<CappedLanePlanPreference>()
    for (item in items) {
        val derived = newCappedLanePlanPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCappedLanePlanPreference(item: CappedLanePlanPreference): String {
    return seasonalCapacityEstimateBucket(item.projectedExposure())
}

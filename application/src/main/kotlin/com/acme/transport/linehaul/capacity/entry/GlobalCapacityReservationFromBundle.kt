package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.linehaul.laneplan.planreservation.AdaptiveLanePlanBundle
import com.acme.transport.linehaul.laneplan.planreservation.classifySeasonalLanePlanReservation

fun summarizeAdaptiveLanePlanBundlesForReservation(items: MutableList<AdaptiveLanePlanBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveGlobalCapacityReservationsFromBundle(items: MutableList<AdaptiveLanePlanBundle>): MutableList<GlobalCapacityReservation> {
    val out = mutableListOf<GlobalCapacityReservation>()
    for (item in items) {
        val derived = newGlobalCapacityReservation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planGlobalCapacityReservation(item: GlobalCapacityReservation): String {
    return classifySeasonalLanePlanReservation(item.measuredValue())
}

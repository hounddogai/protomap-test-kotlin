package com.acme.transport.crewing.roster.score

import com.acme.transport.crewing.availability.availabilityreservation.CappedAvailabilityReservation
import com.acme.transport.crewing.availability.availabilityreservation.classifyCappedAvailabilityReservation

fun summarizeCappedAvailabilityReservationsForGroup(items: MutableList<CappedAvailabilityReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveGlobalRosterGroupsFromReservation(items: MutableList<CappedAvailabilityReservation>): MutableList<GlobalRosterGroup> {
    val out = mutableListOf<GlobalRosterGroup>()
    for (item in items) {
        val derived = newGlobalRosterGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planGlobalRosterGroup(item: GlobalRosterGroup): String {
    return classifyCappedAvailabilityReservation(item.measuredValue())
}

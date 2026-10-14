package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.fleetops.tire.reservation.SeasonalTireMilestone
import com.acme.transport.fleetops.tire.reservation.normalizeLegacyTireReservationCode

fun summarizeSeasonalTireMilestonesForEstimate(items: MutableList<SeasonalTireMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveInspectionEstimatesFromMilestone(items: MutableList<SeasonalTireMilestone>): MutableList<InspectionEstimate> {
    val out = mutableListOf<InspectionEstimate>()
    for (item in items) {
        val derived = newInspectionEstimate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleInspectionEstimate(item: InspectionEstimate): String = normalizeLegacyTireReservationCode(item.code)

package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.fleetops.inspection.summary.InspectionSummary
import com.acme.transport.fleetops.inspection.summary.strictInspectionProposalBucket
import com.acme.transport.fleetops.utilization.summary.UtilizationSummary
import com.acme.transport.fleetops.utilization.summary.classifyUtilizationException

fun summarizeInspectionSummariesForReservation(items: MutableList<InspectionSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveUtilizationReservationsFromSummary(items: MutableList<InspectionSummary>): MutableList<UtilizationReservation> {
    val out = mutableListOf<UtilizationReservation>()
    for (item in items) {
        val derived = newUtilizationReservation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveUtilizationReservation(item: UtilizationReservation): String {
    return strictInspectionProposalBucket(item.effectiveTotal())
}

fun summarizeUtilizationSummariesForReservation(items: MutableList<UtilizationSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveUtilizationReservationsFromSummaryExtra(items: MutableList<UtilizationSummary>): MutableList<UtilizationReservation> {
    val out = mutableListOf<UtilizationReservation>()
    for (item in items) {
        val derived = newUtilizationReservation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveUtilizationReservationExtra(item: UtilizationReservation): String {
    return classifyUtilizationException(item.effectiveTotal())
}

package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.emissions.carbonreport.variance.CarbonReportSegment
import com.acme.transport.emissions.carbonreport.variance.classifyCarbonReportSegment

fun summarizeCarbonReportSegmentsForReservation(items: MutableList<CarbonReportSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveEmissionFactorReservationsFromSegment(items: MutableList<CarbonReportSegment>): MutableList<EmissionFactorReservation> {
    val out = mutableListOf<EmissionFactorReservation>()
    for (item in items) {
        val derived = newEmissionFactorReservation(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeEmissionFactorReservation(item: EmissionFactorReservation): String {
    return classifyCarbonReportSegment(item.effectiveLoad())
}

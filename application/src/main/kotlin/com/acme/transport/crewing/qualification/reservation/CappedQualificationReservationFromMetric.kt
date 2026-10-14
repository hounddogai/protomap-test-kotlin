package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.crewing.qualification.metric.QualificationMetric
import com.acme.transport.crewing.qualification.metric.classifyScheduledQualificationNotice

fun summarizeQualificationMetricsForReservation(items: MutableList<QualificationMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveCappedQualificationReservationsFromMetric(items: MutableList<QualificationMetric>): MutableList<CappedQualificationReservation> {
    val out = mutableListOf<CappedQualificationReservation>()
    for (item in items) {
        val derived = newCappedQualificationReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeCappedQualificationReservationExtra(item: CappedQualificationReservation): String {
    return classifyScheduledQualificationNotice(item.effectiveValue())
}

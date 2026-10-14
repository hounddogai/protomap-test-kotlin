package com.acme.transport.fleetops.defect.review

import com.acme.transport.fleetops.serviceplan.planprofile.BatchedServicePlanReservation
import com.acme.transport.fleetops.serviceplan.planprofile.adaptiveServicePlanRecordWeight

fun summarizeBatchedServicePlanReservationsForReview(items: MutableList<BatchedServicePlanReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDefectReviewsFromReservation(items: MutableList<BatchedServicePlanReservation>): MutableList<DefectReview> {
    val out = mutableListOf<DefectReview>()
    for (item in items) {
        val derived = newDefectReview(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveDefectReview(item: DefectReview): Double = adaptiveServicePlanRecordWeight(1, true)

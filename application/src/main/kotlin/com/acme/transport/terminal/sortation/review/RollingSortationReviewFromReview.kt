package com.acme.transport.terminal.sortation.review

import com.acme.transport.terminal.appointment.estimate.AppointmentReview
import com.acme.transport.terminal.appointment.estimate.appointmentEstimateWeight

fun summarizeAppointmentReviewsForReview(items: MutableList<AppointmentReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveRollingSortationReviewsFromReview(items: MutableList<AppointmentReview>): MutableList<RollingSortationReview> {
    val out = mutableListOf<RollingSortationReview>()
    for (item in items) {
        val derived = newRollingSortationReview(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectRollingSortationReview(item: RollingSortationReview): Double = appointmentEstimateWeight(3, true)

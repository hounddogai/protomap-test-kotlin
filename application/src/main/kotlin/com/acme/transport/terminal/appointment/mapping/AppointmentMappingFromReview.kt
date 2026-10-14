package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.terminal.sortation.review.SortationReview
import com.acme.transport.terminal.sortation.review.normalizeSortationReviewCode

fun summarizeSortationReviewsForMapping(items: MutableList<SortationReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveAppointmentMappingsFromReview(items: MutableList<SortationReview>): MutableList<AppointmentMapping> {
    val out = mutableListOf<AppointmentMapping>()
    for (item in items) {
        val derived = newAppointmentMapping(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleAppointmentMapping(item: AppointmentMapping): String = normalizeSortationReviewCode(item.code)

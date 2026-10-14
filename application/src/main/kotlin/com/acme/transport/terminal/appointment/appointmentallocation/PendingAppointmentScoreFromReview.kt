package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.terminal.dwell.dwellreview.LegacyDwellReview
import com.acme.transport.terminal.dwell.dwellreview.dwellThresholdBucket

fun summarizeLegacyDwellReviewsForScore(items: MutableList<LegacyDwellReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePendingAppointmentScoresFromReview(items: MutableList<LegacyDwellReview>): MutableList<PendingAppointmentScore> {
    val out = mutableListOf<PendingAppointmentScore>()
    for (item in items) {
        val derived = newPendingAppointmentScore(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyPendingAppointmentScore(item: PendingAppointmentScore): String {
    return dwellThresholdBucket(item.weightedLoad())
}

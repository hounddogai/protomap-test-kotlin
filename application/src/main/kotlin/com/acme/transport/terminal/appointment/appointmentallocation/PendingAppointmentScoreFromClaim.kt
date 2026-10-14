package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.terminal.sortation.metric.ScheduledSortationClaim
import com.acme.transport.terminal.sortation.metric.normalizeWeightedSortationEstimateCode

fun summarizeScheduledSortationClaimsForScore(items: MutableList<ScheduledSortationClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun derivePendingAppointmentScoresFromClaim(items: MutableList<ScheduledSortationClaim>): MutableList<PendingAppointmentScore> {
    val out = mutableListOf<PendingAppointmentScore>()
    for (item in items) {
        val derived = newPendingAppointmentScore(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planPendingAppointmentScore(item: PendingAppointmentScore): String {
    return normalizeWeightedSortationEstimateCode(item.code)
}

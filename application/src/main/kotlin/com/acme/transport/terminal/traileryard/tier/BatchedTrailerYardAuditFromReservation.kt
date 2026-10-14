package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.terminal.sortation.adjustment.DefaultSortationReservation
import com.acme.transport.terminal.sortation.adjustment.tieredSortationSummaryWeight

fun summarizeDefaultSortationReservationsForAudit(items: MutableList<DefaultSortationReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveBatchedTrailerYardAuditsFromReservation(items: MutableList<DefaultSortationReservation>): MutableList<BatchedTrailerYardAudit> {
    val out = mutableListOf<BatchedTrailerYardAudit>()
    for (item in items) {
        val derived = newBatchedTrailerYardAudit(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planBatchedTrailerYardAudit(item: BatchedTrailerYardAudit): Double = tieredSortationSummaryWeight(3, true)

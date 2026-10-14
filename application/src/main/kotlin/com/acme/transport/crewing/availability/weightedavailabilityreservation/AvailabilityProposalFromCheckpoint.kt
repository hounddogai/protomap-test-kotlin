package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.crewing.restbreak.checkpoint.ScheduledRestBreakCheckpoint
import com.acme.transport.crewing.restbreak.checkpoint.seasonalRestBreakLedgerWeight

fun summarizeScheduledRestBreakCheckpointsForProposal(items: MutableList<ScheduledRestBreakCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveAvailabilityProposalsFromCheckpoint(items: MutableList<ScheduledRestBreakCheckpoint>): MutableList<AvailabilityProposal> {
    val out = mutableListOf<AvailabilityProposal>()
    for (item in items) {
        val derived = newAvailabilityProposal(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileAvailabilityProposal(item: AvailabilityProposal): Double = seasonalRestBreakLedgerWeight(1, true)

package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.emissions.fuelburn.notice.FuelBurnSummary
import com.acme.transport.emissions.fuelburn.notice.fuelBurnNoticeBucket

fun summarizeFuelBurnSummariesForProposal(items: MutableList<FuelBurnSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveEvChargeProposalsFromSummary(items: MutableList<FuelBurnSummary>): MutableList<EvChargeProposal> {
    val out = mutableListOf<EvChargeProposal>()
    for (item in items) {
        val derived = newEvChargeProposal(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateEvChargeProposal(item: EvChargeProposal): String = fuelBurnNoticeBucket(item.netExposure())

package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.dispute.estimate.DisputeLedger
import com.acme.transport.freightpay.dispute.estimate.disputeEstimateWeight

fun summarizeDisputeLedgersForCheckpoint(items: MutableList<DisputeLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveRateCardCheckpointsFromLedger(items: MutableList<DisputeLedger>): MutableList<RateCardCheckpoint> {
    val out = mutableListOf<RateCardCheckpoint>()
    for (item in items) {
        val derived = newRateCardCheckpoint(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveRateCardCheckpoint(item: RateCardCheckpoint): Double = disputeEstimateWeight(4, true)
